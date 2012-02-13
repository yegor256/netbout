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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

/**
 * Multiplexer of heap updating tasks.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.DoNotUseThreads")
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
     * Tasks to execute.
     */
    private final transient BlockingQueue<Task> queue =
        new LinkedBlockingQueue<Task>();

    /**
     * How many tasks are currently dependants.
     */
    private final transient ConcurrentMap<Urn, AtomicLong> dependants =
        new ConcurrentHashMap<Urn, AtomicLong>();

    /**
     * Stats on performance.
     */
    private final transient DescriptiveStatistics stats =
        new DescriptiveStatistics(100);

    /**
     * Public ctor.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Mux() {
        super(
            Mux.THREADS,
            Mux.THREADS,
            1L,
            TimeUnit.DAYS,
            new LinkedBlockingQueue<Runnable>()
        );
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
        text.append(String.format("%d in queue\n", this.getQueue().size()));
        text.append(String.format("%.2fms avg time\n\n", this.stats.getMean()));
        return text.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        final List<Runnable> killed = this.shutdownNow();
        Logger.info(
            this,
            "#close(): terminated %d tasks (%d remained in the queue)",
            killed.size(),
            this.queue.size()
        );
    }

    /**
     * How long do I need to wait before sending requests?
     * @param who Who is asking
     * @return Estimated number of milliseconds
     */
    public Long eta(final Urn who) {
        Long eta;
        if (this.dependants.containsKey(who)) {
            eta = this.dependants.get(who).get();
            if (eta > 0) {
                eta = this.queue.size() * (long) this.stats.getMean()
                    / this.getActiveCount();
            }
        } else {
            eta = 0L;
        }
        return eta;
    }

    /**
     * Add new task to be executed ASAP.
     * @param task The task to execute
     */
    public void add(final Task task) {
        if (!this.isTerminated() && !this.isShutdown()
            && !this.isTerminating()) {
            synchronized (this) {
                if (this.queue.contains(task)) {
                    Logger.warn(
                        this,
                        "#add('%s'): in the queue already, ignored dup",
                        task
                    );
                } else {
                    this.queue.add(task);
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
    }

    /**
     * Task executing routine.
     */
    private final class Routine implements Runnable {
        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
        public void run() {
            while (true) {
                Task task;
                try {
                    task = Mux.this.queue.take();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(ex);
                }
                for (Urn who : task.dependants()) {
                    synchronized (Mux.this) {
                        Mux.this.dependants.putIfAbsent(who, new AtomicLong());
                        Mux.this.dependants.get(who).incrementAndGet();
                    }
                }
                this.run(task);
                for (Urn who : task.dependants()) {
                    synchronized (Mux.this) {
                        Mux.this.dependants.get(who).decrementAndGet();
                    }
                }
                Mux.this.stats.addValue((double) task.time());
            }
        }
        /**
         * Run one task.
         * @param task The task to run
         */
        @SuppressWarnings("PMD.AvoidCatchingThrowable")
        private void run(final Task task) {
            try {
                task.run();
            // @checkstyle IllegalCatch (1 line)
            } catch (Throwable ex) {
                Mux.this.add(task);
                Logger.warn(
                    this,
                    "#run('%s'): resubmitted because of %[type]s: '%s'",
                    task,
                    ex,
                    ex.getMessage()
                );
                Logger.debug(
                    this,
                    "#run('%s'): resubmit because of:\n%[exception]s",
                    task,
                    ex
                );
            }
        }
    }

}
