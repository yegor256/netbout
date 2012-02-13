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
     * How many tasks are currently dependants.
     */
    private final transient ConcurrentMap<Urn, AtomicLong> dependants =
        new ConcurrentHashMap<Urn, AtomicLong>();

    /**
     * Actively running tasks.
     */
    private final transient ConcurrentMap<Runnable, Long> active =
        new ConcurrentHashMap<Runnable, Long>();

    /**
     * Stats on performance.
     */
    private final transient DescriptiveStatistics stats =
        new DescriptiveStatistics(100);

    /**
     * Public ctor.
     */
    public Mux() {
        super(
            Mux.THREADS,
            Mux.THREADS * 2,
            1L,
            TimeUnit.DAYS,
            (BlockingQueue) new LinkedBlockingQueue<Task>()
        );
        this.prestartAllCoreThreads();
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
            this.getQueue().size()
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
                eta = this.getQueue().size() * (long) this.stats.getMean()
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
    public void submit(final Task task) {
        synchronized (this) {
            if (!this.getQueue().contains(task)) {
                this.getQueue().add(task);
                Logger.debug(
                    this,
                    // @checkstyle LineLength (1 line)
                    "#submit('%s'): #%d in queue, threads=%d, completed=%d, core=%d",
                    task,
                    this.getQueue().size(),
                    this.getActiveCount(),
                    this.getCompletedTaskCount(),
                    this.getCorePoolSize()
                );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    protected void beforeExecute(final Thread thread, final Runnable task) {
        for (Urn who : ((Task) task).dependants()) {
            synchronized (this) {
                this.dependants.putIfAbsent(who, new AtomicLong());
                this.dependants.get(who).incrementAndGet();
            }
        }
        thread.setName(task.toString());
        this.active.put(task, System.currentTimeMillis());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void afterExecute(final Runnable task, final Throwable problem) {
        for (Urn who : ((Task) task).dependants()) {
            synchronized (this) {
                this.dependants.get(who).decrementAndGet();
            }
        }
        this.stats.addValue((double) ((Task) task).time());
        this.active.remove(task);
        if (problem == null) {
            Logger.debug(
                this,
                "#afterExecute('%s'): done",
                task
            );
        } else {
            this.submit(task);
            Logger.warn(
                this,
                "#afterExecute('%s'): resubmitted because of %[type]s: '%s'",
                task,
                problem,
                problem.getMessage()
            );
            Logger.debug(
                this,
                "#afterExecute('%s'): resubmit because of:\n%[exception]s",
                task,
                problem
            );
        }
    }

}
