/**
 * Copyright (c) 2009-2011, netBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code occasionally and without intent to use it, please report this
 * incident to the author by email.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.netbout.inf;

import com.netbout.spi.Urn;
import com.ymock.util.Logger;
import java.io.Closeable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

/**
 * Multiplexer of heap updating tasks.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.DoNotUseThreads")
final class Mux implements Closeable {

    /**
     * How many threads to run in parallel.
     *
     * <p>This number shouldn't be too big, becuase tasks will take just
     * more time to execute, which is not so good. Also keep in mind that
     * the bottleneck here is that database, since every task is mostly working
     * with data retrieval from DB. Thus, pay attention to the number of
     * connections allowed in {@code com.netbout.db.DataSourceBuilder}.
     */
    private static final int THREADS = 20;

    /**
     * Executor service, with a number of threads working in parallel.
     */
    private final transient ExecutorService executor =
        Executors.newFixedThreadPool(Mux.THREADS);

    /**
     * Watcher of Mux.
     */
    private final transient MuxWatcher watcher = new MuxWatcher();

    /**
     * Are we still ready to accept any tasks?
     */
    private final transient AtomicBoolean alive = new AtomicBoolean(true);

    /**
     * How many tasks are currently waiting.
     */
    private final transient ConcurrentMap<Urn, AtomicLong> waiting =
        new ConcurrentHashMap<Urn, AtomicLong>();

    /**
     * Stats on performance.
     */
    private final transient DescriptiveStatistics stats =
        new DescriptiveStatistics(100);

    /**
     * Show some stats.
     * @return The text
     */
    public String statistics() {
        final StringBuilder text = new StringBuilder();
        text.append(String.format("%d identities\n", this.waiting.size()));
        text.append(String.format("%d waiting tasks\n", this.total()));
        text.append(String.format("%.2fms avg time\n\n", this.stats.getMean()));
        text.append("Watcher's stats:\n");
        text.append(this.watcher.statistics());
        return text.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        this.alive.set(false);
        this.watcher.close();
        this.executor.shutdown();
        boolean terminated = false;
        Logger.info(this, "#close(): trying to close Mux tasks gracefully...");
        try {
            // @checkstyle MagicNumber (1 line)
            terminated = this.executor.awaitTermination(5L, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        if (terminated) {
            Logger.info(
                this,
                "#close(): correctly stopped with %d tasks in the queue",
                this.total()
            );
        } else {
            final List<Runnable> killed = this.executor.shutdownNow();
            Logger.warn(
                this,
                "#close(): abruptly terminated %d tasks (%d in the queue)",
                killed.size(),
                this.total()
            );
        }
    }

    /**
     * How long do I need to wait before sending requests?
     * @param who Who is asking
     * @return Estimated number of milliseconds
     */
    public Long eta(final Urn who) {
        Long eta;
        if (this.waiting.containsKey(who)) {
            eta = this.waiting.get(who).get();
            if (eta > 0) {
                eta = this.total() * (long) this.stats.getMean() / Mux.THREADS;
            }
        } else {
            eta = 0L;
        }
        return eta;
    }

    /**
     * Add new task to be executed ASAP.
     * @param who Who is waiting for this task
     * @param task The task to execute
     */
    public void submit(final Set<Urn> who, final Runnable task) {
        if (this.alive.get()) {
            this.watcher.watch(this.executor.submit(new TaskShell(who, task)));
        } else {
            Logger.debug(
                this,
                "#submit(%[list]s, '%s'): Mux is closed, task ignored",
                who,
                task
            );
        }
    }

    /**
     * Wrapper of Task.
     */
    private final class TaskShell implements Runnable {
        /**
         * Who are waiting.
         */
        private final transient Set<Urn> who;
        /**
         * The task to run.
         */
        private final transient Runnable task;
        /**
         * Public ctor.
         * @param urns Who will wait for this result
         * @param tsk The task
         */
        @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
        public TaskShell(final Set<Urn> urns, final Runnable tsk) {
            this.task = tsk;
            this.who = urns;
            for (Urn urn : this.who) {
                Mux.this.waiting.putIfAbsent(urn, new AtomicLong());
                Mux.this.waiting.get(urn).incrementAndGet();
            }
            Logger.debug(
                this,
                "TaskShell(%[list]s, %s): %d in queue",
                urns,
                tsk,
                Mux.this.total()
            );
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("PMD.AvoidCatchingThrowable")
        public void run() {
            final long start = System.currentTimeMillis();
            try {
                this.task.run();
            // @checkstyle IllegalCatchCheck (1 line)
            } catch (Throwable ex) {
                Mux.this.submit(this.who, this.task);
                Logger.warn(
                    this,
                    "run(): '%s' resubmitted because of %[type]s",
                    this.task,
                    ex
                );
                Logger.debug(
                    this,
                    "run(): '%s' resubmit was done because of %[exception]s",
                    this.task,
                    ex
                );
            }
            for (Urn urn : this.who) {
                Mux.this.waiting.get(urn).decrementAndGet();
            }
            Mux.this.stats.addValue(
                (double) System.currentTimeMillis() - start
            );
            Logger.debug(
                this,
                "run(%s): finished, %d still in queue",
                this.task,
                Mux.this.total()
            );
        }
    }

    /**
     * How many waiters are here now (approximate number, since this method
     * is not synchronized)?
     * @return Total number
     */
    private long total() {
        long total = 0;
        for (AtomicLong val : this.waiting.values()) {
            total += val.get();
        }
        return total;
    }

}
