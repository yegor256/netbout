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

import com.jcabi.log.Logger;
import com.jcabi.log.VerboseThreads;
import com.netbout.spi.Urn;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
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
final class Mux implements Closeable {

    /**
     * How many threads to use.
     */
    private static final int THREADS =
        Runtime.getRuntime().availableProcessors() * 4;

    /**
     * The ray.
     */
    private final transient Ray ray;

    /**
     * The store with predicates.
     */
    private final transient Store store;

    /**
     * Running service.
     */
    private final transient ScheduledExecutorService service =
        Executors.newScheduledThreadPool(
            Mux.THREADS,
            new VerboseThreads("mux")
        );

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
     * Futures running.
     */
    private final transient Collection<ScheduledFuture> futures =
        new ArrayList<ScheduledFuture>(Mux.THREADS);

    /**
     * Public ctor.
     * @param iray The ray to use
     * @param str The store to use
     */
    public Mux(final Ray iray, final Store str) {
        this.ray = iray;
        this.store = str;
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    final MuxTask task = Mux.this.queue.take();
                    task.run();
                    for (Urn who : task.dependants()) {
                        Mux.this.dependants.get(who).decrementAndGet();
                    }
                    Mux.this.stats.addValue((double) task.time());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        };
        for (int thread = 0; thread < Mux.THREADS; ++thread) {
            this.futures.add(
                this.service.scheduleWithFixedDelay(
                    runnable, 0L, 1L, TimeUnit.NANOSECONDS
                )
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
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
        for (ScheduledFuture future : this.futures) {
            future.cancel(true);
        }
        this.service.shutdown();
        try {
            // @checkstyle MagicNumber (1 line)
            if (this.service.awaitTermination(10, TimeUnit.SECONDS)) {
                Logger.info(this, "#close(): shutdown() succeeded");
            } else {
                Logger.warn(this, "#close(): shutdown() failed");
                this.service.shutdownNow();
                if (this.service.awaitTermination(1, TimeUnit.MINUTES)) {
                    Logger.info(this, "#close(): shutdownNow() succeeded");
                } else {
                    Logger.error(this, "#close(): failed to stop threads");
                }
            }
        } catch (InterruptedException ex) {
            this.service.shutdownNow();
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
    public long eta(final Urn... who) {
        long eta = 0L;
        for (Urn urn : who) {
            if (this.dependants.containsKey(urn)) {
                eta += this.dependants.get(urn).get();
            }
        }
        if (eta > 0 && this.stats.getN() > 0) {
            eta = this.queue.size() * (long) this.stats.getMean() / Mux.THREADS;
        }
        return eta;
    }

    /**
     * Add new notice to be executed ASAP.
     * @param notice The notice to process
     * @return Who should wait for its processing
     */
    public Set<Urn> add(final Notice notice) {
        final MuxTask task = new MuxTask(notice, this.ray, this.store);
        final Set<Urn> deps = new HashSet<Urn>();
        if (this.queue.contains(task)) {
            Logger.warn(
                this,
                "#add('%s'): in the queue already, ignored dup",
                task
            );
        } else {
            for (Urn who : task.dependants()) {
                if (this.dependants.get(who) == null) {
                    this.dependants.putIfAbsent(who, new AtomicLong());
                }
                this.dependants.get(who).incrementAndGet();
                deps.add(who);
            }
            this.queue.add(task);
            Logger.debug(
                this,
                "#add('%s'): #%d in queue",
                task,
                this.queue.size()
            );
        }
        return deps;
    }

}
