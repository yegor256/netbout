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
package com.netbout.inf.ray.imap.dir;

import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.netbout.inf.Attribute;
import com.netbout.inf.MsgMocker;
import com.netbout.inf.Stash;
import com.netbout.inf.notices.MessagePostedNotice;
import com.netbout.inf.ray.imap.Directory;
import com.netbout.inf.ray.imap.Numbers;
import com.netbout.inf.ray.imap.Reverse;
import com.netbout.spi.Message;
import com.netbout.spi.MessageMocker;
import de.svenjacobs.loremipsum.LoremIpsum;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case of {@link DefaultDirectory}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.DoNotUseThreads")
public final class DefaultDirectoryTest {

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * DefaultDirectory can save numbers to file and restore them back.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void savesAndRestoresNumbers() throws Exception {
        final Directory dir = new DefaultDirectory(
            new File(this.temp.newFolder("foo"), "/some/directory")
        );
        final Numbers numbers = new FastNumbers();
        final long msg = MsgMocker.number();
        numbers.add(msg);
        numbers.add(msg - 1);
        final Attribute attr = new Attribute("some-attr");
        final String value = "some value to use";
        dir.save(attr, value, numbers);
        dir.baseline();
        final Numbers restored = new FastNumbers();
        dir.load(attr, value, restored);
        MatcherAssert.assertThat(restored.next(msg), Matchers.equalTo(msg - 1));
    }

    /**
     * DefaultDirectory can save reverse to file and restore them back.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void savesAndRestoresReverse() throws Exception {
        final Directory dir = new DefaultDirectory(
            this.temp.newFolder("foo-2")
        );
        final Reverse reverse = new SimpleReverse();
        final long msg = MsgMocker.number();
        final String value = "some value 2, \u0433";
        reverse.put(msg, value);
        final Attribute attr = new Attribute("some-attr-2");
        dir.save(attr, reverse);
        dir.baseline();
        final Reverse restored = new SimpleReverse();
        dir.load(attr, restored);
        MatcherAssert.assertThat(restored.get(msg), Matchers.equalTo(value));
    }

    /**
     * DefaultDirectory can convert itself to string.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void convertsItselfToString() throws Exception {
        final Directory dir = new DefaultDirectory(
            this.temp.newFolder("bar-88")
        );
        MatcherAssert.assertThat(
            dir,
            Matchers.hasToString(Matchers.notNullValue())
        );
    }

    /**
     * DefaultDirectory can protect itself from thread-unsafety.
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void resolvesMultiThreadedCalledToStash() throws Exception {
        final Directory dir = new DefaultDirectory(this.temp.newFolder("xx"));
        final Numbers numbers = new FastNumbers();
        final long msg = MsgMocker.number();
        numbers.add(msg);
        numbers.add(msg - 1);
        final Attribute attr = new Attribute("some-attr-value-to-test");
        final String value = new LoremIpsum().getWords();
        dir.save(attr, value, numbers);
        final int threads = 50;
        final ScheduledExecutorService routine =
            Executors.newSingleThreadScheduledExecutor(new VerboseThreads());
        final AtomicInteger errors = new AtomicInteger();
        final Semaphore sem = new Semaphore(1);
        routine.scheduleWithFixedDelay(
            new VerboseRunnable(
                new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        sem.acquire();
                        try {
                            dir.baseline();
                        } finally {
                            sem.release();
                        }
                        return null;
                    }
                },
                true
            ),
            0, 1, TimeUnit.NANOSECONDS
        );
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch latch = new CountDownLatch(threads);
        final ExecutorService svc =
            Executors.newFixedThreadPool(threads, new VerboseThreads());
        final Collection<Future<?>> futures = new ArrayList<Future<?>>(threads);
        for (int thread = 0; thread < threads; ++thread) {
            futures.add(
                svc.submit(
                    // @checkstyle AnonInnerLength (50 lines)
                    new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            final Stash stash = dir.stash();
                            start.await();
                            stash.add(
                                new MessagePostedNotice() {
                                    @Override
                                    public Message message() {
                                        return new MessageMocker().mock();
                                    }
                                }
                            );
                            latch.countDown();
                            return null;
                        }
                    }
                )
            );
        }
        start.countDown();
        for (Future<?> future : futures) {
            future.get();
        }
        svc.shutdown();
        sem.acquire();
        routine.shutdown();
        MatcherAssert.assertThat(
            latch.await(1, TimeUnit.SECONDS),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(errors.get(), Matchers.equalTo(0));
    }

}
