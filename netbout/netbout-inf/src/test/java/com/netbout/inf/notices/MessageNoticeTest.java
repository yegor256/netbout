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
package com.netbout.inf.notices;

import com.netbout.inf.MsgMocker;
import com.netbout.spi.Bout;
import com.netbout.spi.BoutMocker;
import com.netbout.spi.Message;
import com.netbout.spi.MessageMocker;
import com.netbout.spi.Participant;
import com.netbout.spi.ParticipantMocker;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Date;
import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link MessageNotice}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class MessageNoticeTest {

    /**
     * MessageNotice can serialize and de-serialize notices.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void serializesAndDeserializesNotices() throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final Participant dude = new ParticipantMocker()
            .mock();
        final Bout bout = new BoutMocker()
            .withNumber(MsgMocker.number())
            .withDate(new Date())
            .titledAs("some title to serialize")
            .withParticipant(dude)
            .mock();
        final Message message = new MessageMocker()
            .inBout(bout)
            .withDate(new Date())
            .withText("some text to serialize")
            .mock();
        new MessageNotice.Serial().write(
            new MessageNotice() {
                @Override
                public Message message() {
                    return message;
                }
            },
            new DataOutputStream(output)
        );
        final MessageNotice restored = new MessageNotice.Serial().read(
            new DataInputStream(new ByteArrayInputStream(output.toByteArray()))
        );
        MatcherAssert.assertThat(restored.message(), Matchers.equalTo(message));
        MatcherAssert.assertThat(
            restored.message().number(),
            Matchers.equalTo(message.number())
        );
        MatcherAssert.assertThat(
            restored.message().bout().number(),
            Matchers.equalTo(bout.number())
        );
        MatcherAssert.assertThat(
            restored.message().bout().date(),
            Matchers.equalTo(bout.date())
        );
        final Participant found = restored.message().bout()
            .participants().iterator().next();
        MatcherAssert.assertThat(
            found.identity().name(),
            Matchers.equalTo(dude.identity().name())
        );
        MatcherAssert.assertThat(
            found.leader(),
            Matchers.equalTo(dude.leader())
        );
        MatcherAssert.assertThat(
            found.confirmed(),
            Matchers.equalTo(dude.confirmed())
        );
    }

    /**
     * MessageNotice can give correct names to notices.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void givesCorrectNames() throws Exception {
        final String first = new MessageNotice.Serial().nameOf(
            new MessageNotice() {
                @Override
                public Message message() {
                    return new MessageMocker().mock();
                }
            }
        );
        final String second = new MessageNotice.Serial().nameOf(
            new MessageNotice() {
                @Override
                public Message message() {
                    return new MessageMocker().mock();
                }
            }
        );
        MatcherAssert.assertThat(first, Matchers.not(Matchers.equalTo(second)));
    }

    /**
     * MessageNotice can serialize a notice with huge text.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void serializesAndDeserializesWithHugeText() throws Exception {
        // @checkstyle MagicNumber (1 line)
        final String text = RandomStringUtils.random(128 * 1024);
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        new MessageNotice.Serial().write(
            new MessageNotice() {
                @Override
                public Message message() {
                    return new MessageMocker().withText(text).mock();
                }
            },
            new DataOutputStream(output)
        );
        final MessageNotice restored = new MessageNotice.Serial().read(
            new DataInputStream(new ByteArrayInputStream(output.toByteArray()))
        );
        MatcherAssert.assertThat(
            restored.message().text(),
            Matchers.equalTo(text)
        );
    }

}
