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
package com.netbout.notifiers.email;

import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import com.netbout.hub.Hub;
import com.netbout.spi.Bout;
import com.netbout.spi.cpa.Farm;
import com.netbout.spi.cpa.Operation;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;

/**
 * Grab emails from POP3 mail box.
 *
 * <p>At the moment it's gmail.com. In the future we should switch to something
 * more commercial and reliable. For example: fusemail.com, emailhosting.com,
 * fastmail.com (google for "email hosting services").
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Farm
public final class RoutineFarm {

    /**
     * Hub to work with.
     */
    private static Hub hub;

    /**
     * Inject Hub.
     * @param ihub The hub to inject
     * @see com.netbout.rest.AbstractRs#setServletContext(javax.servlet.ServletContext)
     */
    public static void setHub(final Hub ihub) {
        RoutineFarm.hub = ihub;
    }

    /**
     * Routine call.
     * @throws Exception If some problem inside
     */
    @Operation("routine")
    public void routine() throws Exception {
        final Store store = Session.getInstance(new Properties())
            .getStore("pop3s");
        final String user = Manifests.read("Netbout-PopUser");
        try {
            store.connect(
                Manifests.read("Netbout-PopHost"),
                Integer.valueOf(Manifests.read("Netbout-PopPort")),
                user,
                Manifests.read("Netbout-PopPassword")
            );
            final Folder inbox = store.getFolder("inbox");
            inbox.open(Folder.READ_WRITE);
            try {
                final Message[] messages = inbox.getMessages();
                for (Message message : messages) {
                    this.process(message);
                }
                if (messages.length > 0) {
                    Logger.info(
                        this,
                        "#routine(): processed %d email message(s) of '%s'",
                        messages.length,
                        user
                    );
                }
            } finally {
                inbox.close(true);
            }
        } finally {
            store.close();
        }
    }

    /**
     * Process one message.
     * @param message The message to process
     * @throws javax.mail.MessagingException If some problem inside
     * @checkstyle RedundantThrows (3 lines)
     */
    private void process(final Message message)
        throws javax.mail.MessagingException {
        boolean understood = false;
        for (Address email : message.getAllRecipients()) {
            if (this.attempt(message, (InternetAddress) email)) {
                understood = true;
                break;
            }
        }
        if (understood) {
            Logger.info(
                this,
                "#process(..): understood email from '%s'",
                message.getFrom()[0]
            );
        } else {
            Logger.warn(
                this,
                "#process(): ignored and deleted email from '%s'",
                message.getFrom()[0]
            );
        }
        message.setFlag(Flags.Flag.DELETED, true);
    }

    /**
     * Try to understand one destination.
     * @param message The message to process
     * @param email The destination
     * @return Successfully?
     * @throws javax.mail.MessagingException If some problem inside
     * @checkstyle RedundantThrows (3 lines)
     */
    private boolean attempt(final Message message,
        final InternetAddress email) throws javax.mail.MessagingException {
        boolean success = false;
        try {
            new AnchorEmail(email, RoutineFarm.hub)
                .bout()
                .post(new EmailMessage(message).text());
            success = true;
        } catch (BrokenAnchorException ex) {
            Logger.warn(
                this,
                "#process(): message from '%s' to %[list]s ignored: %s",
                message.getFrom()[0],
                message.getAllRecipients(),
                ex.getMessage()
            );
        } catch (MessageParsingException ex) {
            Logger.warn(
                this,
                "#process(): message from '%s' can't be parsed, ignored: %s",
                message.getFrom()[0],
                ex.getMessage()
            );
        } catch (Bout.MessagePostException ex) {
            Logger.warn(
                this,
                "#process(): message from '%s' failed to post: %[exception]s",
                message.getFrom()[0],
                ex
            );
        }
        return success;
    }

}
