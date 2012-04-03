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

import com.ymock.util.Logger;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;

/**
 * Dummy transport.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class TransportMocker extends Transport {

    /**
     * Public ctor.
     * @param session The session
     * @param name The name
     */
    public TransportMocker(final Session session, final URLName name) {
        super(session, name);
        Logger.info(
            this,
            "#TransportMocker('%[type]s', '%s'): instantiated",
            session,
            name
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(final Message message, final Address[] addrs) {
        try {
            Logger.info(
                this,
                // @checkstyle LineLength (1 line)
                "#sendMessage(..):\n  From: %[list]s\n  To: %[list]s\n  CC:%s\n  Reply-to: %[list]s\n  Subject: %s\n  Text: %s",
                message.getFrom(),
                message.getRecipients(Message.RecipientType.TO),
                message.getRecipients(Message.RecipientType.CC),
                message.getReplyTo(),
                message.getSubject(),
                message.getContent()
            );
        } catch (javax.mail.MessagingException ex) {
            throw new IllegalArgumentException(ex);
        } catch (java.io.IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * {@inheritDoc}
     * @checkstyle ParameterNumber (4 lines)
     */
    @Override
    public void connect(final String host, final int port, final String user,
        final String password) {
        Logger.info(
            this,
            "#connect('%s', %d, '%s', '%s')",
            host,
            port,
            user,
            password
        );
    }

}
