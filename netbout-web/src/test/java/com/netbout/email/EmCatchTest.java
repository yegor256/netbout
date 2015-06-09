/**
 * Copyright (c) 2009-2015, netbout.com
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

import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link EmCatch}.
 * @author Erim Erturk (erimerturk@gmail.com)
 * @version $Id$
 * @since 2.15
 */
public final class EmCatchTest {

    /**
     * EmCatch should read inbox periodically and call Action.run.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void readInboxPeriodically() throws Exception {
        final GreenMail greenMail = new GreenMail(ServerSetupTest.POP3);
        greenMail.start();
        final ServerSetup serverSetup = greenMail.getPop3().getServerSetup();
        final String mailUser = "to";
        // @checkstyle MagicNumberCheck (1 line)
        final String from = "from@localhost.com";
        // @checkstyle LocalFinalVariableNameCheck (1 line)
        final String to = "to@localhost.com";
        final String password = "soooosecret";
        final String subject = GreenMailUtil.random();
        final String body = GreenMailUtil.random();
        final GreenMailUser user = greenMail.setUser(mailUser, password);
        new EmCatch(
            // @checkstyle AnonInnerLengthCheck (22 lines)
            new EmCatch.Action() {
                @Override
                public void run(final Message msg) {
                    MatcherAssert.assertThat(msg, Matchers.notNullValue());
                    try {
                        MatcherAssert.assertThat(
                            msg.getSubject(),
                            Matchers.equalTo(subject)
                        );
                        MatcherAssert.assertThat(
                            msg.getFrom()[0].toString(),
                            Matchers.equalTo(from)
                        );
                        MatcherAssert.assertThat(
                            msg.getAllRecipients()[0].toString(),
                            Matchers.equalTo(to)
                        );
                    } catch (final MessagingException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            },
            mailUser,
            password,
            serverSetup.getBindAddress(),
            serverSetup.getPort(),
            // @checkstyle MagicNumberCheck (1 line)
            500L
        );
        final MimeMessage message = GreenMailUtil.createTextEmail(
            to,
            from,
            subject,
            body,
            serverSetup
        );
        user.deliver(message);
        MatcherAssert.assertThat(
            greenMail.getReceivedMessages().length,
            Matchers.equalTo(1)
        );
        // @checkstyle MagicNumberCheck (1 line)
        Thread.sleep(1000);
        greenMail.stop();
    }

}
