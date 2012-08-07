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

import com.netbout.inf.MsgMocker;
import com.netbout.inf.Notice;
import com.netbout.inf.Stash;
import com.netbout.inf.notices.MessagePostedNotice;
import com.netbout.spi.Message;
import com.netbout.spi.MessageMocker;
import java.io.File;
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
        final long number = MsgMocker.number();
        final Notice notice = new MessagePostedNotice() {
            @Override
            public Message message() {
                return new MessageMocker()
                    .withText("some text to index")
                    .withNumber(number)
                    .mock();
            }
        };
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
        final Stash stash = new DefaultStash(
            this.temp.newFolder("foo-55")
        );
        MatcherAssert.assertThat(
            stash,
            Matchers.hasToString(Matchers.notNullValue())
        );
    }

}
