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

import com.jcabi.email.Envelope;
import com.jcabi.email.Postman;
import com.netbout.mock.MkBase;
import com.netbout.spi.Alias;
import com.netbout.spi.Bout;
import java.io.ByteArrayOutputStream;
import javax.mail.Message;
import javax.mail.internet.MimeMultipart;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * Test case for {@link BoutInviteMail}.
 *
 * @author Mesut Ozen (mesutozen36@gmail.com)
 * @version $Id$
 * @since 2.17
 */
public final class BoutInviteMailTest {

    /**
     * BoutInviteMail can send email.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void sendEmailWithInviteKey() throws Exception {
        final String content =
            "You are invited into the Netbout click on the link to";
        final Postman postman = Mockito.mock(Postman.class);
        final BoutInviteMail mail = new BoutInviteMail(postman);
        final MkBase base = new MkBase();
        final Alias alias = new EmAlias(base.randomAlias(), postman);
        final String email = "mesutozen36@gmail.com";
        final String urn = "urn:mesutozen36@gmail.com:mesutozen36-gmail-com";
        final Bout bout = alias.inbox().bout(alias.inbox().start());
        mail.send(email, urn, bout);
        final ArgumentCaptor<Envelope> captor =
            ArgumentCaptor.forClass(Envelope.class);
        Mockito.verify(postman).send(captor.capture());
        final Message msg = captor.getValue().unwrap();
        MatcherAssert.assertThat(msg.getAllRecipients().length, Matchers.is(1));
        MatcherAssert.assertThat(
            msg.getAllRecipients()[0].toString(),
            Matchers.equalTo(email)
        );
        MatcherAssert.assertThat(msg.getSubject(), Matchers.startsWith("#1: "));
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MimeMultipart.class.cast(msg.getContent()).writeTo(baos);
        MatcherAssert.assertThat(
            baos.toString(), Matchers.containsString(content)
        );
    }
}

