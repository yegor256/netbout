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
     * Public ctor.
     */
    public Mux() {
        super(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors() * 2,
            1L,
            TimeUnit.MINUTES,
            (BlockingQueue) new LinkedBlockingQueue<Task>()
        );
    }

    /**
     * Show some stats.
     * @return The text
     */
    public String statistics() {
        final StringBuilder text = new StringBuilder();
        text.append(String.format("%d identities\n", this.waiting.size()));
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
        if (this.waiting.containsKey(who)) {
            eta = this.waiting.get(who).get();
            if (eta > 0) {
                eta = this.getQueue().size() * (long) this.stats.getMean()
                    / (1 + this.getActiveCount());
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
            }
        }
        Logger.debug(
            this,
            "#submit('%s'): #%d in queue, threads=%d, completed=%d, core=%d",
            task,
            this.getQueue().size(),
            this.getActiveCount(),
            this.getCompletedTaskCount(),
            this.getCorePoolSize()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    protected void beforeExecute(final Thread thread, final Runnable task) {
        for (Urn who : ((Task) task).dependants()) {
            synchronized (this) {
                this.waiting.putIfAbsent(who, new AtomicLong());
                this.waiting.get(who).incrementAndGet();
            }
        }
        thread.setName(task.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void afterExecute(final Runnable task, final Throwable problem) {
        for (Urn who : ((Task) task).dependants()) {
            synchronized (this) {
                this.waiting.get(who).decrementAndGet();
            }
        }
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
