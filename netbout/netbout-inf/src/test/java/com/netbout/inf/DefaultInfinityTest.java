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
import com.netbout.inf.notices.MessagePostedNotice;
import com.netbout.spi.Bout;
import com.netbout.spi.BoutMocker;
import com.netbout.spi.Message;
import com.netbout.spi.MessageMocker;
import com.netbout.spi.Urn;
import com.netbout.spi.UrnMocker;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link DefaultInfinity}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class DefaultInfinityTest {

    /**
     * DefaultInfinity can find messages.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsMessageJustPosted() throws Exception {
        final Infinity inf = new DefaultInfinity(new FolderMocker().mock());
        final Bout bout = new BoutMocker()
            .withParticipant(new UrnMocker().mock())
            .mock();
        final Message msg = new MessageMocker()
            .withText("some text to index")
            .withNumber(MsgMocker.number())
            .inBout(bout)
            .mock();
        final Urn[] deps = inf.see(
            new MessagePostedNotice() {
                @Override
                public Message message() {
                    return msg;
                }
            }
        ).toArray(new Urn[0]);
        int total = 0;
        while (inf.eta(deps) != 0) {
            TimeUnit.MILLISECONDS.sleep(1);
            Logger.debug(this, "eta=%[nano]s", inf.eta(deps));
            // @checkstyle MagicNumber (1 line)
            if (++total > 1000) {
                throw new IllegalStateException("time out");
            }
        }
        final String query = String.format(
            "(pos 0)",
            msg.number()
        );
        MatcherAssert.assertThat(
            inf.messages(query),
            Matchers.<Long>iterableWithSize(1)
        );
        inf.close();
    }

    /**
     * DefaultInfinity can restore its state from files.
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void restoresItselfFromFileSystem() throws Exception {
        final Folder folder = new FolderMocker().mock();
        final Infinity inf = new DefaultInfinity(folder);
        final Bout bout = new BoutMocker()
            .withParticipant(new UrnMocker().mock())
            .mock();
        final Message msg = new MessageMocker()
            .withText("Jeffrey Lebowski, \u0443\u0440\u0430! How are you?")
            .withNumber(MsgMocker.number())
            .inBout(bout)
            .mock();
        final Urn[] deps = inf.see(
            new MessagePostedNotice() {
                @Override
                public Message message() {
                    return msg;
                }
            }
        ).toArray(new Urn[0]);
        int total = 0;
        while (inf.eta(deps) != 0) {
            TimeUnit.MILLISECONDS.sleep(1);
            // @checkstyle MagicNumber (1 line)
            if (++total > 1000) {
                throw new IllegalStateException("time out 2");
            }
        }
        inf.flush();
        inf.close();
        for (int attempt = 0; attempt <= 2; ++attempt) {
            final Infinity restored = new DefaultInfinity(folder);
            MatcherAssert.assertThat(
                restored.messages("(matches 'Jeffrey')"),
                Matchers.<Long>iterableWithSize(Matchers.greaterThan(0))
            );
            restored.close();
        }
    }

    /**
     * DefaultInfinity can convert itselt to string.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void convertsItseltToString() throws Exception {
        MatcherAssert.assertThat(
            new DefaultInfinity(new FolderMocker().mock()),
            Matchers.hasToString(Matchers.notNullValue())
        );
    }

}
