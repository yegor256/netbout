/**
 * Copyright (c) 2009-2011, netBout.com
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
package com.netbout.notifiers.email;

import java.util.Arrays;
import java.util.Collection;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;

/**
 * Test case for {@link EmailMessage}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@RunWith(Parameterized.class)
public final class EmailMessageTest {

    /**
     * Name of file with email text.
     */
    private final transient String resource;

    /**
     * Public ctor.
     * @param name Name of resource
     */
    public EmailMessageTest(final String name) {
        this.resource = name;
    }

    /**
     * List of email texts.
     * @return The list of them
     */
    @Parameters
    public static Collection<Object[]> emails() {
        return Arrays.asList(
            new Object[][] {
                {"raw/gmail.eml"},
            }
        );
    }

    /**
     * EmailMessage can extract pure text from a message.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void extractsPureTextFromMessage() throws Exception {
        final String raw = this.raw();
        final BodyPart body = Mockito.mock(BodyPart.class);
        Mockito.doReturn(raw).when(body).getContent();
        Mockito.doReturn(MediaType.TEXT_PLAIN).when(body).getContentType();
        final Multipart parts = Mockito.mock(Multipart.class);
        Mockito.doReturn(1).when(parts).getCount();
        Mockito.doReturn(body).when(parts).getBodyPart(0);
        final Message msg = Mockito.mock(Message.class);
        Mockito.doReturn(parts).when(msg).getContent();
        MatcherAssert.assertThat(
            new EmailMessage(msg).text(),
            Matchers.allOf(
                Matchers.startsWith("first line\n"),
                Matchers.endsWith("\nlast line")
            )
        );
    }

    /**
     * Get raw text of the email.
     * @return The raw email text
     * @throws Exception If there is some problem inside
     */
    private String raw() throws Exception {
        return IOUtils.toString(
            this.getClass().getResourceAsStream(this.resource)
        ).replace("\n", EmailMessage.CRLF);
    }

}
