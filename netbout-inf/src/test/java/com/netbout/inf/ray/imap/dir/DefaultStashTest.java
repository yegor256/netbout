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
package com.netbout.inf.ray.imap.dir;

import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.netbout.inf.Notice;
import com.netbout.inf.Stash;
import com.netbout.inf.notices.MessagePostedNotice;
import com.netbout.inf.notices.MessagePostedNoticeMocker;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case of {@link DefaultStash}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
public final class DefaultStashTest {

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * DefaultStash can save notices and find them later.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void savesNoticesAndRetrievesThem() throws Exception {
        final File dir = this.temp.newFolder("foo");
        final Notice notice = new MessagePostedNoticeMocker().mock();
        final Stash first = new DefaultStash(dir);
        first.add(notice);
        first.close();
        final Stash second = new DefaultStash(dir);
        MatcherAssert.assertThat(
            second.iterator().hasNext(),
            Matchers.is(true)
        );
        final MessagePostedNotice restored = MessagePostedNotice.class.cast(
            second.iterator().next()
        );
        MatcherAssert.assertThat(
            restored.message().text(),
            Matchers.containsString("text to index")
        );
        second.remove(restored);
        second.close();
        MatcherAssert.assertThat(
            new DefaultStash(dir).iterator().hasNext(),
            Matchers.is(true)
        );
    }

    /**
     * DefaultStash can convert itself to string.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void convertsItselfToString() throws Exception {
        final Stash stash = new DefaultStash(this.temp.newFolder("foo-55"));
        MatcherAssert.assertThat(
            stash,
            Matchers.hasToString(Matchers.notNullValue())
        );
    }

    /**
     * DefaultStash can add and delete in parallel.
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings("PMD.DoNotUseThreads")
    public void addsAndDeletesInParallel() throws Exception {
        final Stash stash = new DefaultStash(this.temp.newFolder("foo-9"));
        final int threads = Runtime.getRuntime().availableProcessors() * 10;
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(threads);
        final ExecutorService svc =
            Executors.newFixedThreadPool(threads, new VerboseThreads());
        final Runnable runnable = new VerboseRunnable(
            // @checkstyle AnonInnerLength (50 lines)
            new Callable<Void>() {
                public Void call() throws Exception {
                    start.await();
                    final Notice notice =
                        new MessagePostedNoticeMocker().mock();
                    stash.add(notice);
                    stash.remove(notice);
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
        MatcherAssert.assertThat(
            done.await(1, TimeUnit.MINUTES),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(stash, Matchers.<Notice>emptyIterable());
        svc.shutdown();
        stash.close();
    }

    /**
     * DefaultStash can reject a call if not a directory.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsIfDirectoryIsNotValid() throws Exception {
        new DefaultStash(this.temp.newFile("foo-9898"));
    }

    /**
     * DefaultStash can ignore deleted notices during copying.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void ignoresDeletedDuringCopying() throws Exception {
        final Notice notice = new MessagePostedNoticeMocker().mock();
        final Stash first = new DefaultStash(this.temp.newFolder("f-1"));
        first.add(notice);
        first.remove(notice);
        final Stash second = new DefaultStash(this.temp.newFolder("f-2"));
        first.copyTo(second);
        first.close();
        MatcherAssert.assertThat(
            second,
            Matchers.<Notice>emptyIterable()
        );
        second.close();
    }

    /**
     * DefaultStash can copy undone notices.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void copyesNoticesWhichAreNotDone() throws Exception {
        final Notice notice = new MessagePostedNoticeMocker().mock();
        final Stash first = new DefaultStash(this.temp.newFolder("x-1"));
        first.add(notice);
        final Stash second = new DefaultStash(this.temp.newFolder("x-2"));
        first.copyTo(second);
        first.close();
        MatcherAssert.assertThat(
            second,
            Matchers.not(Matchers.<Notice>emptyIterable())
        );
        second.close();
    }

}
