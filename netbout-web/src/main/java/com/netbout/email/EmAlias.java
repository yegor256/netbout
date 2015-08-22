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

import com.google.common.base.Joiner;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.email.Envelope;
import com.jcabi.email.Postman;
import com.jcabi.email.enclosure.EnHTML;
import com.jcabi.email.stamp.StRecipient;
import com.jcabi.email.stamp.StSender;
import com.jcabi.email.stamp.StSubject;
import com.netbout.rest.Markdown;
import com.netbout.spi.Alias;
import com.netbout.spi.Bout;
import com.netbout.spi.Inbox;
import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import javax.crypto.KeyGenerator;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.codec.binary.Base64;
import org.takes.facets.auth.Identity;
import org.takes.facets.auth.codecs.CcAES;
import org.takes.facets.auth.codecs.Codec;

/**
 * Email Alias.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.12
 * @checkstyle ClassDataAbstractionCouplingCheck (200 lines)
 */
@Immutable
@Loggable(Loggable.DEBUG)
@ToString(of = "origin")
@EqualsAndHashCode(of = "origin")
@SuppressWarnings("PMD.TooManyMethods")
final class EmAlias implements Alias {

    /**
     * Encryption type.
     *
     */
    private static final String ENCRYPTION = "AES";

    /**
     * Key length.
     */
    private static final int KEY_LENGTH = 128;

    /**
     * Mail content for invited user by email.
     */
    private static final String MAIL_CONTENT =
        "You are invited into the Netbout click on the link to register";

    /**
     * Original.
     */
    private final transient Alias origin;

    /**
     * Postman.
     */
    private final transient Postman postman;

    /**
     * Public ctor.
     * @param org Origin
     * @param pst Postman
     */
    EmAlias(final Alias org, final Postman pst) {
        this.origin = org;
        this.postman = new Postman() {
            @Override
            public void send(final Envelope envelope) throws IOException {
                pst.send(
                    new Envelope.MIME(envelope).with(
                        new StSender(org.name(), "no-reply@netbout.com")
                    )
                );
            }
        };
    }

    @Override
    public String name() throws IOException {
        return this.origin.name();
    }

    @Override
    public URI photo() throws IOException {
        return this.origin.photo();
    }

    @Override
    public Locale locale() throws IOException {
        return this.origin.locale();
    }

    @Override
    public void photo(final URI uri) throws IOException {
        this.origin.photo(uri);
    }

    @Override
    public String email() throws IOException {
        return this.origin.email();
    }

    @Override
    public void email(final String email) throws IOException {
        this.origin.email(email);
    }

    @Override
    public void email(final String email, final String urn, final Bout bout)
        throws IOException {
        this.origin.email(email);
        this.send(email, urn, bout);
    }

    @Override
    public Inbox inbox() throws IOException {
        return new EmInbox(this.origin.inbox(), this.postman, this.name());
    }

    /**
     * Create invite key by urn using CcAES.
     * @param urn Urn
     * @return Encoded urn
     * @throws IOException if fails
     */
    private String encode(final String urn)
        throws IOException {
        try {
            final KeyGenerator generator = KeyGenerator.getInstance(ENCRYPTION);
            generator.init(KEY_LENGTH);
            final byte[] key = generator.generateKey().getEncoded();
            final Codec codec = new CcAES(
                new Codec() {
                    @Override
                    public Identity decode(final byte[] bytes)
                        throws IOException {
                        return new Identity.Simple(new String(bytes));
                    }
                    @Override
                    public byte[] encode(final Identity identity)
                        throws IOException {
                        return identity.urn().getBytes();
                    }
                },
                key
            );
            final byte[] encode = codec.encode(new Identity.Simple(urn));
            return Base64.encodeBase64URLSafeString(encode);
        } catch (final NoSuchAlgorithmException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Send an email with register link which is like.
     * http://www.netbout.com/b/<bout_number>?invite=<invite-key>
     * @param email Email
     * @param urn Urn
     * @param bout Bout
     * @throws IOException if fails
     */
    private void send(final String email, final String urn, final Bout bout)
        throws IOException {
        this.postman.send(
            new Envelope.MIME()
                .with(new StRecipient(email))
                .with(
                    new StSubject(
                        String.format(
                            "#%d: %s",
                            bout.number(),
                            bout.title()
                        )
                    )
                )
                .with(
                    new EnHTML(
                        Joiner.on('\n').join(
                            new Markdown(MAIL_CONTENT).html(), "<br/>",
                            String.format(
                                "http://www.netbout.com/b/%d?invite=%s",
                                bout.number(),
                                this.encode(urn)
                            ),
                            "<p style=\"color:#C8C8C8;font-size:2px;\">",
                            String.format("%d</p>", System.nanoTime()),
                            new GmailViewAction(bout.number()).xml()
                        )
                    )
                )
        );
    }
}
