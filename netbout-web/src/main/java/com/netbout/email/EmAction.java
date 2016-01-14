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

import com.jcabi.urn.URN;
import com.netbout.spi.Base;
import java.io.IOException;
import java.util.regex.Pattern;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;

/**
 * Post the message into the bout as the author.
 *
 * @author Dmitry Zaytsev (dmitry.zaytsev@gmail.com)
 * @version $Id$
 * @since 2.17
 */
public final class EmAction implements EmCatch.Action {
    /**
     * Line separator used in emails.
     * @link <a href="http://www.ietf.org/rfc/rfc2822.txt">RFC2822</a>
     */
    public static final String CRLF = "\r\n";

    /**
     * Base.
     */
    private final transient Base base;

    /**
     * Ctor.
     * @param bse The base
     */
    public EmAction(final Base bse) {
        this.base = bse;
    }

    @Override
    public void run(final Message msg) throws IOException {
        try {
            for (final Address addr
                : msg.getRecipients(Message.RecipientType.TO)) {
                final String adr = addr.toString();
                if (adr.endsWith("@reply.netbout.com")) {
                    final String[] split = EmCatch.decrypt(
                        adr.substring(0, adr.indexOf('@'))
                    ).split("\\|");
                    this.base.user(URN.create(split[0])).aliases()
                        .iterate().iterator().next().inbox()
                        .bout(Long.parseLong(split[1]))
                        .messages().post(this.text(msg));
                }
            }
        } catch (final MessagingException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Get its visible part.
     * @param msg Message
     * @return The text of it
     * @throws IOException If some problem inside
     */
    private String text(final Message msg) throws IOException {
        final String raw = EmAction.raw(msg);
        final StringBuilder text = new StringBuilder();
        for (final String line : raw.split(Pattern.quote(EmAction.CRLF))) {
            text.append(line).append('\n');
        }
        return text.toString().trim();
    }
    /**
     * Get raw text.
     * @param msg Message
     * @return The text of it
     * @throws IOException If some problem inside
     */
    private static String raw(final Message msg) throws IOException {
        try {
            final Object body = msg.getContent();
            if (!(body instanceof Multipart)) {
                throw new IOException("body is not Multipart");
            }
            final Multipart parts = (Multipart) body;
            for (int pos = 0; pos < parts.getCount(); ++pos) {
                final BodyPart part = parts.getBodyPart(pos);
                if (part.getContentType().startsWith("text/plain")) {
                    return part.getContent().toString();
                }
            }
        } catch (final MessagingException ex) {
            throw new IOException(ex);
        }
        throw new IOException("no plain/text part found");
    }
}
