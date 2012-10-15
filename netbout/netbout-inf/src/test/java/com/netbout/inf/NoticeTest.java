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

import com.netbout.inf.notices.MessagePostedNotice;
import com.netbout.spi.Bout;
import com.netbout.spi.BoutMocker;
import com.netbout.spi.Message;
import com.netbout.spi.MessageMocker;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link Notice}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class NoticeTest {

    /**
     * Notice can serialize and de-serialize notices.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void serializesAndDeserializesNotices() throws Exception {
        final Message message = new MessageMocker().mock();
        final byte[] bytes = new Notice.SerializableNotice(
            new MessagePostedNotice() {
                @Override
                public Message message() {
                    return message;
                }
                @Override
                public Bout bout() {
                    return new BoutMocker().mock();
                }
            }
        ).serialize();
        MatcherAssert.assertThat(
            MessagePostedNotice.class.cast(
                Notice.SerializableNotice.deserialize(bytes)
            ).message().number(),
            Matchers.equalTo(message.number())
        );
    }

    /**
     * Notice can give correct names to notices.
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
        final String first = new Notice.SerializableNotice(notice).toString();
        final String second = new Notice.SerializableNotice(notice).toString();
        MatcherAssert.assertThat(first, Matchers.not(Matchers.equalTo(second)));
    }

}
