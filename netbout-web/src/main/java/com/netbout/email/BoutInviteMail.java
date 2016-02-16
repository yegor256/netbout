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

import com.google.common.base.Joiner;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.email.Envelope;
import com.jcabi.email.Postman;
import com.jcabi.email.enclosure.EnHTML;
import com.jcabi.email.stamp.StRecipient;
import com.jcabi.email.stamp.StSubject;
import com.jcabi.manifests.Manifests;
import com.netbout.rest.Markdown;
import com.netbout.spi.Bout;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

/**
 * Bout Invite Email.
 *
 * @author Mesut Ozen (mesutozen36@gmail.com)
 * @version $Id$
 * @since 2.17
 */
@Immutable
@Loggable(Loggable.DEBUG)
@ToString(of = "postman")
@EqualsAndHashCode(of = "postman")
final class BoutInviteMail {

    /**
     * Encryptor.
     */
    private static final StandardPBEStringEncryptor ENC =
        new StandardPBEStringEncryptor();

    /**
     * Mail content for invited user by email.
     */
    private static final String MAIL_CONTENT =
        "You are invited into the Netbout click on the link to register";

    /**
     * Postman.
     * Postman.
     */
    private final transient Postman postman;

    static {
        BoutInviteMail.ENC.setPassword(
            Manifests.read("Netbout-BoutInviteSecret")
        );
    }

    /**
     * Public ctor.
     * @param pst Postman
     */
    BoutInviteMail(final Postman pst) {
        this.postman = pst;
    }

    /**
     * Send an email with register link which is like.
     * {@code
     * http://www.netbout.com/b/<bout_number>?invite=<invite-key>
     * }
     *
     * @param email Email
     * @param urn Urn
     * @param bout Bout
     * @throws IOException if fails
     */
    public void send(final String email, final String urn, final Bout bout)
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
                            new Markdown.Default().html(
                                BoutInviteMail.MAIL_CONTENT
                            ),
                            "<br/>",
                            String.format(
                                Manifests.read("Netbout-Site")
                                    .concat("/b/%d?invite=%s"),
                            bout.number(),
                            encrypt(urn)
                            ),
                            "<p style=\"color:#C8C8C8;font-size:2px;\">",
                            String.format("%d</p>", System.nanoTime()),
                            new GmailViewAction(bout.number()).xml()
                        )
                    )
                )
        );
    }

    /**
     * Encrypt text string.
     * @param text String to encrypt
     * @return Encrypted string
     */
    private static String encrypt(final String text) {
        return BoutInviteMail.ENC.encrypt(text);
    }
}
