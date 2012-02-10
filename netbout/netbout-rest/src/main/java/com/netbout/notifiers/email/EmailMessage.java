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
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * One email message.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class EmailMessage {

    /**
     * Line separator used in emails.
     * @link <a href="http://www.ietf.org/rfc/rfc2822.txt">RFC2822</a>
     */
    public static final String CRLF = "\r\n";

    /**
     * Stoppers of content.
     */
    private static final Collection<Pattern> STOPPERS =
        new ArrayList<Pattern>();

    /**
     * Build patterns.
     */
    static {
        final String[] regexs = new String[] {
            "from:.*",
            ".*<.*@.*>",
            "sent via netbout: https?://.*",
            "-+original\\s+message-+",
            ">.*",
            "-+",
            "\\u8212",
            // @checkstyle LineLength (1 line)
            "on [a-z]{3}, [a-z]{3} \\d{1,2}, \\d{4} at \\d{1,2}:\\d{2} (am|pm),.*",
        };
        for (String regex : regexs) {
            EmailMessage.STOPPERS.add(
                Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
            );
        }
    }

    /**
     * The message.
     */
    private final transient Message message;

    /**
     * Public ctor.
     * @param msg The msg
     */
    public EmailMessage(final Message msg) {
        this.message = msg;
    }

    /**
     * Get its visible part.
     * @return The text of it
     * @throws MessageParsingException If some problem inside
     */
    public String text() throws MessageParsingException {
        final String raw = this.raw();
        final StringBuilder text = new StringBuilder();
        boolean found = false;
        for (String line : raw.split(Pattern.quote(this.CRLF))) {
            final String polished = line.replaceAll("[\\s\r\t]+", " ").trim();
            for (Pattern pattern : this.STOPPERS) {
                if (pattern.matcher(polished).matches()) {
                    found = true;
                    break;
                }
            }
            if (found) {
                Logger.info(
                    this,
                    "#text(): email stopper found at line: \"%s\"",
                    line
                );
                break;
            }
            text.append(polished).append("\n");
        }
        if (!found) {
            Logger.warn(
                this,
                "#text(): failed to find a stopper line in:\n%s",
                StringEscapeUtils.escapeJava(raw)
            );
        }
        return text.toString().trim();
    }

    /**
     * Get raw text.
     * @return The text of it
     * @throws MessageParsingException If some problem inside
     */
    private String raw() throws MessageParsingException {
        try {
            final Object body = this.message.getContent();
            if (!(body instanceof Multipart)) {
                throw new MessageParsingException("body is not Multipart");
            }
            final Multipart parts = (Multipart) body;
            for (int pos = 0; pos < parts.getCount(); pos += 1) {
                final BodyPart part = parts.getBodyPart(pos);
                if (part.getContentType().startsWith(MediaType.TEXT_PLAIN)) {
                    return part.getContent().toString();
                }
            }
        } catch (java.io.IOException ex) {
            throw new MessageParsingException(ex);
        } catch (javax.mail.MessagingException ex) {
            throw new MessageParsingException(ex);
        }
        throw new MessageParsingException("no plain/text part found");
    }

}
