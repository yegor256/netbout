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
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
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
@SuppressWarnings({ "PMD.DoNotUseThreads", "PMD.TestClassWithoutTestCases" })
public final class MuxTest {

    /**
     * Mux can run tasks in parallel.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void runsTasksInParallel() throws Exception {
        final Ray ray = new RayMocker().mock();
        final Store store = new StoreMocker().mock();
        final Mux mux = new Mux(ray, store);
        final AtomicInteger received = new AtomicInteger();
        final AtomicInteger pushed = new AtomicInteger();
        Mockito.doAnswer(
            new Answer<Void>() {
                public Void answer(final InvocationOnMock invocation)
                    throws Exception {
                    TimeUnit.SECONDS.sleep(1);
                    received.incrementAndGet();
                    return null;
                }
            }
        ).when(store).see(Mockito.eq(ray), Mockito.any(Notice.class));
        final int threads = Runtime.getRuntime().availableProcessors() * 50;
        final ScheduledExecutorService svc =
            Executors.newScheduledThreadPool(threads, new VerboseThreads());
        final Runnable runnable = new VerboseRunnable(
            new Callable<Void>() {
                public Void call() throws Exception {
                    mux.add(
                        new MessagePostedNotice() {
                            @Override
                            public Message message() {
                                return new MessageMocker().inBout(
                                    new BoutMocker().withParticipant(
                                        new UrnMocker().mock()
                                    ).mock()
                                ).mock();
                            }
                        }
                    );
                    pushed.incrementAndGet();
                    return null;
                }
            },
            true
        );
        svc.scheduleAtFixedRate(runnable, 0, 1L, TimeUnit.MILLISECONDS);
        TimeUnit.SECONDS.sleep(1);
        svc.shutdown();
        MuxTest.waitFor(mux);
        mux.close();
        MatcherAssert.assertThat(
            received.get(),
            Matchers.equalTo(pushed.get())
        );
        MatcherAssert.assertThat(
            pushed.get(),
            Matchers.greaterThan(0)
        );
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
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void acceptsManyNotices() throws Exception {
        final Ray ray = new RayMocker().mock();
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
        final int total = 1000;
        for (int num = 0; num < total; ++num) {
            mux.add(
                new MessagePostedNotice() {
                    @Override
                    public Message message() {
                        return new MessageMocker().inBout(
                            new BoutMocker().withParticipant(
                                new UrnMocker().mock()
                            ).mock()
                        ).mock();
                    }
                }
            );
        }
        MuxTest.waitFor(mux);
        mux.close();
        MatcherAssert.assertThat(
            received.get(),
            Matchers.equalTo(total)
        );
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

}
