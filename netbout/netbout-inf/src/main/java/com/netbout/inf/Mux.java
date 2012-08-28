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
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.netbout.spi.Urn;
import com.rexsl.core.Manifests;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
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
     * How often to flush, in ms.
     */
    private static final long PERIOD = Mux.delay();

    /**
     * How many threads to use (more than the number of processors, because
     * most of the time in every MuxTask will spent on I/O operations, like
     * getting data from the database).
     */
    private static final int THREADS =
        Runtime.getRuntime().availableProcessors() * 2;

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
    private final transient Collection<ScheduledFuture<?>> futures =
        new ArrayList<ScheduledFuture<?>>(Mux.THREADS);

    /**
     * Semaphore, that holds locks for every actively working task (and
     * doesn't allow new tasks to start if there are not enough locks).
     */
    private final transient Semaphore semaphore = new Semaphore(Mux.THREADS);

    /**
     * When {@link #flush()} was called last time.
     */
    private final transient AtomicLong flushed =
        new AtomicLong(System.currentTimeMillis());

    /**
     * Public ctor.
     * @param iray The ray to use
     * @param str The store to use
     * @throws IOException If some I/O problem inside
     */
    public Mux(final Ray iray, final Store str) throws IOException {
        this.ray = iray;
        this.store = str;
        int stashed = 0;
        for (Notice notice : this.ray.stash()) {
            this.add(notice);
            this.ray.stash().remove(notice);
            ++stashed;
        }
        // @checkstyle AnonInnerLength (30 lines)
        final Callable<?> callable = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    Mux.this.flush();
                    Mux.this.semaphore.acquire();
                    final MuxTask task =
                        Mux.this.queue.poll(1, TimeUnit.MINUTES);
                    if (task != null) {
                        final double time = task.call();
                        for (Urn who : task.dependants()) {
                            Mux.this.dependants.get(who).decrementAndGet();
                        }
                        Mux.this.stats.addValue(time);
                    }
                } finally {
                    Mux.this.semaphore.release();
                }
                return null;
            }
        };
        for (int thread = 0; thread < Mux.THREADS; ++thread) {
            this.futures.add(
                this.service.scheduleWithFixedDelay(
                    new VerboseRunnable(callable, true),
                    0L, 1L, TimeUnit.NANOSECONDS
                )
            );
        }
        Logger.info(
            this,
            "#Mux(..): %d notice(s) from stash, %d threads, %[ms]s delay",
            stashed,
            Mux.THREADS,
            Mux.PERIOD
        );
    }

    /**
     * How long do I need to wait before sending requests?
     * @param who Who is asking
     * @return Estimated number of nanoseconds
     */
    public long eta(final Urn... who) {
        long eta = 0L;
        if (who.length > 0) {
            for (Urn urn : who) {
                if (this.dependants.containsKey(urn)) {
                    eta += this.dependants.get(urn).get();
                }
            }
        } else {
            for (AtomicLong val : this.dependants.values()) {
                eta += val.get();
            }
        }
        if (eta > 0 && this.stats.getN() > 0) {
            eta = Math.max(
                this.queue.size() * (long) this.stats.getMean() / Mux.THREADS,
                1L
            );
        }
        return eta;
    }

    /**
     * Add new notice to be executed ASAP.
     * @param notice The notice to process
     * @return Who should wait for its processing
     * @throws IOException If some IO problem inside
     */
    public Set<Urn> add(final Notice notice) throws IOException {
        this.ray.stash().add(notice);
        final MuxTask task = new MuxTask(notice, this.ray, this.store);
        final Set<Urn> deps = new HashSet<Urn>();
        if (this.queue.contains(task)) {
            Logger.debug(
                this,
                "#add('%s'): in the queue already, ignored dup",
                task
            );
        } else {
            for (Urn who : task.dependants()) {
                this.dependants.putIfAbsent(who, new AtomicLong());
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
                "flushed %[ms]s ago\n",
                System.currentTimeMillis() - this.flushed.get()
            )
        );
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
        for (ScheduledFuture<?> future : this.futures) {
            future.cancel(true);
        }
        this.service.shutdown();
        try {
            if (this.service.awaitTermination(1, TimeUnit.SECONDS)) {
                Logger.debug(this, "#close(): shutdown() succeeded");
            } else {
                Logger.warn(this, "#close(): shutdown() failed");
                this.service.shutdownNow();
                if (this.service.awaitTermination(1, TimeUnit.SECONDS)) {
                    Logger.info(this, "#close(): shutdownNow() succeeded");
                } else {
                    Logger.error(this, "#close(): failed to stop threads");
                }
            }
        } catch (InterruptedException ex) {
            this.service.shutdownNow();
            Thread.currentThread().interrupt();
            Logger.warn(
                this,
                "#close(): shutdownNow() due to %[exception]s",
                ex
            );
        }
        Logger.info(
            this,
            "#close(): %d remained in the queue",
            this.queue.size()
        );
    }

    /**
     * Flush mux to disc.
     *
     * <p>Before flushing we "acquire" all semaphores, to prevent any changes
     * to the Mux during this flushing period.
     *
     * <p>The method is synchronized in order to avoid simultaneous execution
     * of it in a few threads. The first call will check time and it if's
     * suitable will do the flushing and will RESET the time marker.
     *
     * @throws InterruptedException If interrupted
     */
    private void flush() throws InterruptedException {
        synchronized (this.flushed) {
            if (System.currentTimeMillis() - this.flushed.get() > Mux.PERIOD) {
                this.semaphore.acquire(Mux.THREADS);
                try {
                    this.ray.flush();
                } catch (java.io.IOException ex) {
                    throw new IllegalArgumentException(ex);
                } finally {
                    this.semaphore.release(Mux.THREADS);
                    this.flushed.set(System.currentTimeMillis());
                }
            }
        }
    }

    /**
     * How long to wait, in ms, between flushes to disc.
     * @return Time in milliseconds
     */
    private static long delay() {
        long delay;
        final String prop = "Netbout-InfDelay";
        if (Manifests.exists(prop)) {
            delay = Long.valueOf(Manifests.read(prop));
        } else {
            // @checkstyle MagicNumber (1 line)
            delay = 15 * 60 * 1000L;
        }
        return delay;
    }

}
