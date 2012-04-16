/**
 * Copyright (c) 2009-2012, Netbout.com
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

/**
 * Multiplexer of notices.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings({
    "PMD.DoNotUseThreads", "PMD.AvoidInstantiatingObjectsInLoops"
})
final class Mux extends ThreadPoolExecutor implements Closeable {

    /**
     * How many threads to use.
     *
     * <p>I don't know how to calculate this number correctly. Let's try to
     * experiment.
     */
    private static final int THREADS =
        Runtime.getRuntime().availableProcessors() * 4;

    /**
     * The store with predicates.
     */
    private final transient Store store;

    /**
     * Tasks to execute.
     */
    private final transient BlockingQueue<MuxTask> queue =
        new LinkedBlockingQueue<MuxTask>();

    /**
     * How many notices every identity has now in pending status.
     */
    private final transient ConcurrentMap<Urn, AtomicLong> dependants =
        new ConcurrentHashMap<Urn, AtomicLong>();

    /**
     * Stats on notice processing performance.
     */
    private final transient DescriptiveStatistics stats =
        new DescriptiveStatistics(5000);

    /**
     * Public ctor.
     * @param str The store to use
     */
    public Mux(final Store str) {
        super(
            Mux.THREADS,
            Mux.THREADS,
            1L,
            TimeUnit.DAYS,
            new LinkedBlockingQueue<Runnable>(),
            new ThreadFactory() {
                private int num;
                @Override
                public Thread newThread(final Runnable runnable) {
                    return new Thread(
                        runnable,
                        String.format("mux-pool-%d", ++this.num)
                    );
                }
            }
        );
        this.store = str;
        this.prestartAllCoreThreads();
        for (int thread = 0; thread < Mux.THREADS; thread += 1) {
            this.submit(new Routine());
        }
    }

    /**
     * Show some stats.
     * @return The text
     */
    public String statistics() {
        final StringBuilder text = new StringBuilder();
        text.append(String.format("%d identities\n", this.dependants.size()));
        for (ConcurrentMap.Entry<Urn, AtomicLong> entry
            : this.dependants.entrySet()) {
            if (entry.getValue().get() != 0) {
                text.append(
                    String.format(
                        "  %s: %d\n",
                        entry.getKey(),
                        entry.getValue().get()
                    )
                );
            }
        }
        text.append(String.format("%d in the queue\n", this.queue.size()));
        text.append(
            Logger.format(
                "%[nano]s avg time\n",
                (long) this.stats.getMean()
            )
        );
        return text.toString();
    }

    /**
     * {@inheritDoc}
     *
     * @see <a href="http://docs.oracle.com/javase/6/docs/api/java/util/concurrent/ExecutorService.html">Example</a>
     */
    @Override
    public void close() {
        this.shutdown();
        try {
            // @checkstyle MagicNumber (1 line)
            if (this.awaitTermination(10, TimeUnit.SECONDS)) {
                Logger.info(this, "#close(): shutdown() succeeded");
            } else {
                Logger.warn(this, "#close(): shutdown() failed");
                this.shutdownNow();
                if (this.awaitTermination(1, TimeUnit.MINUTES)) {
                    Logger.info(this, "#close(): shutdownNow() succeeded");
                } else {
                    Logger.error(this, "#close(): failed to stop threads");
                }
            }
        } catch (InterruptedException ex) {
            this.shutdownNow();
            Thread.currentThread().interrupt();
        }
        Logger.info(
            this,
            "#close(): %d remained in the queue",
            this.queue.size()
        );
    }

    /**
     * How long do I need to wait before sending requests?
     * @param who Who is asking
     * @return Estimated number of nanoseconds
     */
    public long eta(final Urn who) {
        long eta;
        final int count = this.getActiveCount();
        if (this.dependants.containsKey(who) && count > 0) {
            eta = this.dependants.get(who).get();
            if (eta > 0) {
                eta = this.queue.size() * (long) this.stats.getMean() / count;
            }
        } else {
            eta = 0L;
        }
        return eta;
    }

    /**
     * Add new notice to be executed ASAP.
     * @param notice The notice to process
     */
    public void add(final Notice notice) {
        if (!this.isTerminated() && !this.isShutdown()
            && !this.isTerminating()) {
            final MuxTask task = new MuxTask(notice, this.store);
            if (this.queue.contains(task)) {
                Logger.debug(
                    this,
                    "#add('%s'): in the queue already, ignored dup",
                    task
                );
            } else {
                this.queue.add(task);
                for (Urn who : task.dependants()) {
                    this.dependants.putIfAbsent(who, new AtomicLong());
                    this.dependants.get(who).incrementAndGet();
                }
                Logger.debug(
                    this,
                    // @checkstyle LineLength (1 line)
                    "#add('%s'): #%d in queue, threads=%d, completed=%d, core=%d",
                    task,
                    this.queue.size(),
                    this.getActiveCount(),
                    this.getCompletedTaskCount(),
                    this.getCorePoolSize()
                );
            }
        }
    }

    /**
     * Task executing routine.
     */
    private final class Routine implements Runnable {
        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            while (true) {
                MuxTask task;
                try {
                    task = Mux.this.queue.take();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
                this.run(task);
                for (Urn who : task.dependants()) {
                    Mux.this.dependants.get(who).decrementAndGet();
                }
                Mux.this.stats.addValue((double) task.time());
            }
        }
        /**
         * Run one task.
         * @param task The task to run
         */
        @SuppressWarnings("PMD.AvoidCatchingThrowable")
        private void run(final MuxTask task) {
            try {
                task.run();
            // @checkstyle IllegalCatch (1 line)
            } catch (Throwable ex) {
                Mux.this.add(task.notice());
                Logger.warn(
                    this,
                    "#run('%s'): resubmitted because of: %[exception]s",
                    task,
                    ex
                );
            }
        }
    }

}
