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
import com.netbout.inf.notices.MessagePostedNotice;
import com.netbout.spi.BoutMocker;
import com.netbout.spi.Message;
import com.netbout.spi.MessageMocker;
import com.netbout.spi.UrnMocker;
import com.rexsl.core.Manifests;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test case of {@link Mux}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings({ "PMD.DoNotUseThreads", "PMD.TooManyMethods" })
public final class MuxTest {

    /**
     * Test-wide incrementor.
     */
    private static final AtomicLong NUMBER = new AtomicLong();

    /**
     * Snapshot of manifests.
     */
    private static byte[] snapshot;

    /**
     * Pre-configure Mux.
     */
    @BeforeClass
    public static void setupMux() {
        MuxTest.snapshot = Manifests.snapshot();
        Manifests.inject("Netbout-InfDelay", "500");
    }

    /**
     * Un-configure Mux.
     */
    @AfterClass
    public static void tearDownMux() {
        Manifests.revert(MuxTest.snapshot);
    }

    /**
     * Mux can accept notices from parallel sources.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void acceptsNoticesInParallel() throws Exception {
        final Ray ray = MuxTest.slowRay(2);
        final Store store = new StoreMocker().mock();
        final Mux mux = new Mux(ray, store);
        final AtomicInteger received = new AtomicInteger();
        Mockito.doAnswer(
            new Answer<Void>() {
                public Void answer(final InvocationOnMock invocation)
                    throws Exception {
                    received.incrementAndGet();
                    return null;
                }
            }
        ).when(store).see(Mockito.eq(ray), Mockito.any(Notice.class));
        final int threads = Runtime.getRuntime().availableProcessors() * 50;
        final ExecutorService svc =
            Executors.newFixedThreadPool(threads, new VerboseThreads());
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(threads);
        final Runnable runnable = new VerboseRunnable(
            new Callable<Void>() {
                public Void call() throws Exception {
                    start.await(1, TimeUnit.SECONDS);
                    MuxTest.pushTo(mux);
                    done.countDown();
                    return null;
                }
            },
            true
        );
        for (int thread = 0; thread < threads; ++thread) {
            svc.submit(runnable);
        }
        start.countDown();
        done.await(1, TimeUnit.MINUTES);
        MuxTest.waitFor(mux);
        svc.shutdown();
        mux.close();
        MatcherAssert.assertThat(received.get(), Matchers.equalTo(threads));
    }

    /**
     * Mux can render statistics.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void rendersStatistics() throws Exception {
        MatcherAssert.assertThat(
            new Mux(new RayMocker().mock(), new StoreMocker().mock()),
            Matchers.hasToString(Matchers.notNullValue())
        );
    }

    /**
     * Mux can accept many notices.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void acceptsManyNotices() throws Exception {
        final Ray ray = MuxTest.slowRay(2);
        final Store store = new StoreMocker().mock();
        final Mux mux = new Mux(ray, store);
        final AtomicInteger received = new AtomicInteger();
        Mockito.doAnswer(
            new Answer<Void>() {
                public Void answer(final InvocationOnMock invocation) {
                    received.incrementAndGet();
                    return null;
                }
            }
        ).when(store).see(Mockito.eq(ray), Mockito.any(Notice.class));
        final int total = 100;
        for (int num = 0; num < total; ++num) {
            MuxTest.pushTo(mux);
        }
        MuxTest.waitFor(mux);
        mux.close();
        MatcherAssert.assertThat(
            received.get(),
            Matchers.equalTo(total)
        );
    }

    /**
     * Mux can accept and process notices asynchronously.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void acceptsAndProcessesNoticesAsynchronously() throws Exception {
        final Ray ray = MuxTest.slowRay(25);
        final Store store = new StoreMocker().mock();
        final Mux mux = new Mux(ray, store);
        final AtomicInteger received = new AtomicInteger();
        Mockito.doAnswer(
            new Answer<Void>() {
                public Void answer(final InvocationOnMock invocation) {
                    received.incrementAndGet();
                    return null;
                }
            }
        ).when(store).see(Mockito.eq(ray), Mockito.any(Notice.class));
        final int total = 100;
        for (int num = 0; num < total; ++num) {
            MuxTest.pushTo(mux);
        }
        MatcherAssert.assertThat(
            received.get(),
            Matchers.lessThan(total)
        );
        mux.close();
    }

    /**
     * Wait for eta of zero.
     * @param mux The mux
     * @throws InterruptedException If any
     * @checkstyle MagicNumber (20 lines)
     */
    private static void waitFor(final Mux mux) throws InterruptedException {
        int cycles = 0;
        while (mux.eta() != 0) {
            TimeUnit.MILLISECONDS.sleep(100);
            Logger.debug(MuxTest.class, "eta=%[nano]s", mux.eta());
            if (++cycles > 500) {
                throw new IllegalStateException(
                    String.format(
                        "time out after %d 100ms cycles of waiting",
                        cycles
                    )
                );
            }
        }
    }

    /**
     * Create slow Ray.
     * @param seconds How many seconds to sleep on every flush
     * @return Ray, slow
     * @throws Exception If there is some problem inside
     */
    private static Ray slowRay(final int seconds) throws Exception {
        final Ray ray = new RayMocker().mock();
        Mockito.doAnswer(
            new Answer<Void>() {
                public Void answer(final InvocationOnMock invocation)
                    throws Exception {
                    TimeUnit.SECONDS.sleep(seconds);
                    Logger.debug(
                        this,
                        "#answer(): flushed after %dsec of sleep",
                        seconds
                    );
                    return null;
                }
            }
        ).when(ray).flush();
        return ray;
    }

    /**
     * Add new notice to the Mux.
     * @param mux The mux
     * @throws Exception If there is some problem inside
     */
    private static void pushTo(final Mux mux) throws Exception {
        mux.add(
            new MessagePostedNotice() {
                @Override
                public Message message() {
                    return new MessageMocker()
                        .withNumber(MuxTest.NUMBER.incrementAndGet())
                        .inBout(
                            new BoutMocker()
                                .withNumber(MuxTest.NUMBER.incrementAndGet())
                                .withParticipant(new UrnMocker().mock())
                                .mock()
                        ).mock();
                }
            }
        );
    }

}
