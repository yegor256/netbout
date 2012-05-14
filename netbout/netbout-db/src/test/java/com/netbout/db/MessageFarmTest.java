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
package com.netbout.db;

import com.netbout.spi.Urn;
import java.util.Date;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link MessageFarm}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class MessageFarmTest {

    /**
     * Farm to work with.
     */
    private final transient MessageFarm farm = new MessageFarm();

    /**
     * MessageFarm can check for message existence.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void addsMessageToBoutAndChecksForExistence() throws Exception {
        final Long bout = new BoutRowMocker().mock();
        final Long message = new MessageRowMocker(bout).mock();
        MatcherAssert.assertThat(
            "exists",
            this.farm.checkMessageExistence(bout, message)
        );
    }

    /**
     * MessageFarm can find bout number by message number.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsBoutNumberByMessageNumber() throws Exception {
        final Long bout = new BoutRowMocker().mock();
        final Long message = new MessageRowMocker(bout).mock();
        MatcherAssert.assertThat(
            this.farm.getBoutOfMessage(message),
            Matchers.equalTo(bout)
        );
    }

    /**
     * MessageFarm can set and read message date.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void setsAndReadsMessageDate() throws Exception {
        final Long message =
            new MessageRowMocker(new BoutRowMocker().mock()).mock();
        final Date date = new Date();
        this.farm.changedMessageDate(message, date);
        MatcherAssert.assertThat(
            this.farm.getMessageDate(message),
            Matchers.equalTo(date)
        );
    }

    /**
     * MessageFarm can set and read message author.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void setsAndReadsMessageAuthor() throws Exception {
        final Long message =
            new MessageRowMocker(new BoutRowMocker().mock()).mock();
        final Urn author = new IdentityRowMocker().mock();
        this.farm.changedMessageAuthor(message, author);
        MatcherAssert.assertThat(
            this.farm.getMessageAuthor(message),
            Matchers.equalTo(author)
        );
    }

    /**
     * MessageFarm can set and read message text.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void setsAndReadsMessageText() throws Exception {
        final Long message =
            new MessageRowMocker(new BoutRowMocker().mock()).mock();
        final String text = "\u043F\u0440\u0438\u0432\u0435\u0442";
        this.farm.changedMessageText(message, text);
        MatcherAssert.assertThat(
            this.farm.getMessageText(message),
            Matchers.equalTo(text)
        );
    }

    /**
     * MessageFarm can find a chunk of messages.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsChunkOfMessages() throws Exception {
        final Long bout = new BoutRowMocker().mock();
        final Long message = new MessageRowMocker(bout).mock();
        MatcherAssert.assertThat(
            this.farm.getMessagesChunk(message - 1, 2L),
            Matchers.hasItem(message)
        );
    }

}
