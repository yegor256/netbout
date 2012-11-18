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
package com.netbout.inf.notices;

import com.jcabi.urn.URN;
import com.netbout.spi.Bout;
import com.netbout.spi.BoutMocker;
import com.netbout.spi.Message;
import com.netbout.spi.MessageMocker;
import com.netbout.spi.ParticipantMocker;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link MessagePostedNotice}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class MessagePostedNoticeTest {

    /**
     * MessagePostedNotice can give correct names to notices.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void givesCorrectNames() throws Exception {
        final MessagePostedNotice notice = new MessagePostedNotice() {
            @Override
            public Message message() {
                return new MessageMocker().mock();
            }
            @Override
            public Bout bout() {
                return new BoutMocker().mock();
            }
        };
        final String first = new MessagePostedNotice.Serial().nameOf(notice);
        final String second = new MessagePostedNotice.Serial().nameOf(notice);
        MatcherAssert.assertThat(first, Matchers.not(Matchers.equalTo(second)));
    }

    /**
     * MessagePostedNotice can preserve deps.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void preservesDepsAfterSerialization() throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final Bout bout = new BoutMocker()
            .withParticipant(new ParticipantMocker().mock())
            .mock();
        final Message message = new MessageMocker().mock();
        final MessagePostedNotice notice = new MessagePostedNotice() {
            @Override
            public Message message() {
                return message;
            }
            @Override
            public Bout bout() {
                return bout;
            }
        };
        new MessagePostedNotice.Serial().write(
            notice,
            new DataOutputStream(output)
        );
        final MessagePostedNotice restored =
            new MessagePostedNotice.Serial().read(
                new DataInputStream(
                    new ByteArrayInputStream(output.toByteArray())
                )
            );
        MatcherAssert.assertThat(
            new MessagePostedNotice.Serial().deps(restored),
            Matchers.contains(
                new MessagePostedNotice.Serial().deps(notice)
                    .toArray(new URN[0])
            )
        );
    }

}
