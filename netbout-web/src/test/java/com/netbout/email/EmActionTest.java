/**
 * Copyright (c) 2009-2016, netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netbout Inc. located at www.netbout.com.
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
package com.netbout.email;

import com.jcabi.email.Enclosure;
import com.jcabi.email.Envelope;
import com.jcabi.email.enclosure.EnPlain;
import com.jcabi.email.stamp.StRecipient;
import com.jcabi.email.stamp.StSender;
import com.jcabi.immutable.Array;
import com.jcabi.urn.URN;
import com.netbout.mock.MkBase;
import com.netbout.spi.Bout;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link EmAction}.
 * @author Dmitry Zaytsev (dmitry.zaytsev@gmail.com)
 * @version $Id$
 * @since 2.17
 * @checkstyle ClassDataAbstractionCouplingCheck (200 lines)
 */
public final class EmActionTest {

    /**
     * EmAction can send a message into the bout.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void sendsMessage() throws Exception {
        final String alias = "test";
        final String urn = "urn:test:1";
        final MkBase base = new MkBase();
        final Bout bout = base.randomBout();
        base.user(new URN(urn)).aliases().add(alias);
        bout.friends().invite(alias);
        final String text = "Hello world";
        new EmAction(base).run(
            new Envelope.MIME(
                new Array<>(
                    new StSender("Yegor Bugayenko <yegor@jcabi.com>"),
                    new StRecipient(
                        String.format(
                            "%s%s",
                            EmCatch.encrypt(
                                String.format(
                                    "%s|%d",
                                    urn,
                                    bout.number()
                                )
                            ),
                            "@reply.netbout.com"
                        )
                    )
                ),
                new Array<Enclosure>(
                    new EnPlain(text)
                )
            ).unwrap()
        );
        MatcherAssert.assertThat(
            bout.messages().iterate(),
            Matchers.<com.netbout.spi.Message>iterableWithSize(1)
        );
        MatcherAssert.assertThat(
            bout.messages().iterate().iterator().next().text(),
            Matchers.containsString(text)
        );
    }
}
