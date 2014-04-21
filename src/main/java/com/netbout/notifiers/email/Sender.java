/**
 * Copyright (c) 2009-2014, Netbout.com
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
package com.netbout.notifiers.email;

import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

/**
 * Email sender.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class Sender {

    /**
     * Mail sending session.
     */
    private final transient Session session;

    /**
     * Public ctor.
     */
    public Sender() {
        final Properties props = new Properties();
        props.put("mail.smtp.auth", true);
        this.session = Session.getInstance(props);
    }

    /**
     * Create new message.
     * @return The message
     */
    public Message newMessage() {
        return new MimeMessage(this.session);
    }

    /**
     * Send message by email.
     * @param message The message
     */
    public void send(final Message message) {
        try {
            final Transport transport = this.session.getTransport("smtps");
            transport.connect(
                Manifests.read("Netbout-SmtpHost"),
                Integer.valueOf(Manifests.read("Netbout-SmtpPort")),
                Manifests.read("Netbout-SmtpUser"),
                Manifests.read("Netbout-SmtpPassword")
            );
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            Logger.info(
                this,
                "Email sent to '%s'",
                message.getAllRecipients()[0]
            );
        } catch (javax.mail.NoSuchProviderException ex) {
            throw new IllegalArgumentException(ex);
        } catch (javax.mail.MessagingException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

}
