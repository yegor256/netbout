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
 * this code accidentally and without intent to use it, please report this
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
import com.jcabi.manifests.Manifests;
import com.jcabi.urn.URN;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.validation.constraints.NotNull;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

/**
 * Multiplexer of notices.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.DoNotUseThreads")
final class Mux implements Closeable {

    /**
     * How many threads to use (more than the number of processors, because
     * most of the time in every MuxTask will spent on I/O operations, like
     * getting data from the database).
     */
    private static final int THREADS =
        Runtime.getRuntime().availableProcessors() * 2;

    /**
     * How often to flush, in ms.
     */
    private final transient long period = Mux.delay();

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
    private final transient MuxQueue queue = new MuxQueue();

    /**
     * How many notices every identity has now in pending status.
     */
    private final transient ConcurrentMap<URN, AtomicLong> dependants =
        new ConcurrentHashMap<URN, AtomicLong>();

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
     *
     * <p>What's important is that the semaphore has to "fair", because
     * sometimes we need one flag from it, sometimes all.
     */
    private final transient Semaphore semaphore =
        new Semaphore(Mux.THREADS, true);

    /**
     * When {@link #flush()} was called last time.
     */
    private final transient AtomicLong flushed =
        new AtomicLong(System.currentTimeMillis());

    /**
     * How many notices were added after recent flush?
     */
    private final transient AtomicLong notices = new AtomicLong();

    /**
     * Patronized runnables.
     */
    private final transient PatronizedRunnables patronized =
        new PatronizedRunnables(1000L);

    /**
     * Public ctor.
     *
     * <p>We don't report exceptions in the runnable, mostly in order to
     * avoid garbage messages in log.
     *
     * @param iray The ray to use
     * @param str The store to use
     * @throws IOException If some I/O problem inside
     */
    public Mux(@NotNull final Ray iray,
        @NotNull final Store str) throws IOException {
        this.ray = iray;
        this.store = str;
        this.apply(this.ray.stash());
        final Runnable runnable = new VerboseRunnable(
            new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return Mux.this.dispatch();
                }
            },
            true
        );
        for (int thread = 0; thread < Mux.THREADS; ++thread) {
            this.futures.add(
                this.service.scheduleWithFixedDelay(
                    this.patronized.patronize(runnable),
                    0L, 1L, TimeUnit.NANOSECONDS
                )
            );
        }
        Logger.info(
            this,
            "#Mux(..): %d threads, %[ms]s delay",
            Mux.THREADS,
            this.period
        );
    }

    /**
     * How long do I need to wait before sending requests?
     * @param who Who is asking
     * @return Estimated number of nanoseconds
     */
    public long eta(@NotNull final URN... who) {
        long eta = 0L;
        if (who.length > 0) {
            for (URN urn : who) {
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
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Set<URN> add(@NotNull final Notice notice) throws IOException {
        this.ray.stash().add(notice);
        final MuxTask task = new MuxTask(notice, this.ray, this.store);
        final Set<URN> deps = new HashSet<URN>();
        if (this.queue.contains(task)) {
            Logger.debug(
                this,
                "#add('%s'): in the queue already, ignored dup",
                task
            );
        } else {
            for (URN who : task.dependants()) {
                this.dependants.putIfAbsent(who, new AtomicLong());
                this.dependants.get(who).incrementAndGet();
                deps.add(who);
            }
            this.queue.add(task);
            Logger.debug(
                this,
                "#add('%s'): #%d in queue (#%d after recent flush)",
                task,
                this.queue.size(),
                this.notices.incrementAndGet()
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
        for (ConcurrentMap.Entry<URN, AtomicLong> entry
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
    public void close() throws IOException {
        try {
            this.semaphore.acquire(Mux.THREADS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException(ex);
        }
        for (ScheduledFuture<?> future : this.futures) {
            future.cancel(true);
        }
        this.service.shutdown();
        try {
            if (this.service.awaitTermination(1, TimeUnit.SECONDS)) {
                Logger.info(this, "#close(): shutdown() succeeded");
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
        this.patronized.close();
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
     * @throws Exception If some error inside
     */
    private void flush() throws Exception {
        synchronized (this.flushed) {
            if (System.currentTimeMillis() - this.flushed.get() > this.period
                // @checkstyle MagicNumber (1 line)
                || this.notices.get() > 1000) {
                this.semaphore.acquire(Mux.THREADS);
                try {
                    this.ray.flush();
                    this.notices.set(0L);
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

    /**
     * Dispatch the next task.
     * @return TRUE if something was dispatched or FALSE if it's a waste call
     * @throws Exception If something goes wrong
     */
    private boolean dispatch() throws Exception {
        boolean dispatched = false;
        try {
            this.flush();
            if (this.semaphore.tryAcquire(1, TimeUnit.SECONDS)) {
                try {
                    final MuxTask task = this.queue.poll(1, TimeUnit.SECONDS);
                    if (task != null) {
                        final double time = task.call();
                        for (URN who : task.dependants()) {
                            this.dependants.get(who).decrementAndGet();
                        }
                        this.stats.addValue(time);
                        dispatched = true;
                    }
                } finally {
                    this.semaphore.release();
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return dispatched;
    }

    /**
     * Apply stash to this Mux.
     * @param stash The stash to apply
     * @throws IOException If fails
     */
    private void apply(final Stash stash) throws IOException {
        final long start = System.currentTimeMillis();
        int imported = 0;
        for (Notice notice : stash) {
            this.add(notice);
            stash.remove(notice);
            ++imported;
            // @checkstyle MagicNumber (1 line)
            if (imported > 0 && imported % 100 == 0) {
                Logger.info(
                    this,
                    "#apply(..): imported %d notice(s), %[ms]s spent, %d/sec",
                    imported,
                    System.currentTimeMillis() - start,
                    // @checkstyle MagicNumber (1 line)
                    imported * 1000 / (System.currentTimeMillis() - start)
                );
            }
        }
        Logger.info(
            this,
            "#apply(%s): %d notice(s) imported in %[ms]s",
            stash,
            imported,
            System.currentTimeMillis() - start
        );
    }

}
