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
package com.netbout.mock;

import com.netbout.spi.Bout;
import com.netbout.spi.Message;
import com.netbout.spi.Messages;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link MkMessages}.
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.15
 */
public final class MkMessagesTest {

    /**
     * MkMessages can post and read messages.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void postsAndReadsMessages() throws Exception {
        final Bout bout = new MkBase().randomBout();
        final Messages messages = bout.messages();
        messages.post("how are you?");
        MatcherAssert.assertThat(
            messages.iterate(),
            Matchers.<Message>iterableWithSize(1)
        );
        final Message message = messages.iterate().iterator().next();
        MatcherAssert.assertThat(
            message.date(),
            Matchers.notNullValue()
        );
    }

    /**
     * MkMessages can change update attribut on Bout.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void changesUpdateAttribute() throws Exception {
        final Bout bout = new MkBase().randomBout();
        final Messages messages = bout.messages();
        final Long last = bout.updated().getTime();
        messages.post("hi");
        final Long pause = 100L;
        Thread.sleep(pause);
        MatcherAssert.assertThat(
            bout.updated().getTime(), Matchers.greaterThan(last)
        );
    }
}
