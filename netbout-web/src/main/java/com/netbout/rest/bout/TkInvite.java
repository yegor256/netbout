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
package com.netbout.rest.bout;

import com.jcabi.urn.URN;
import com.netbout.rest.RqAlias;
import com.netbout.spi.Base;
import com.netbout.spi.Bout;
import com.netbout.spi.Friends;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import javax.xml.bind.DatatypeConverter;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.auth.RqWithAuth;
import org.takes.facets.flash.RsFlash;
import org.takes.facets.forward.RsFailure;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqForm;

/**
 * Invite a friend to the bout.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.14
 * @checkstyle ClassDataAbstractionCouplingCheck (210 lines)
 */
final class TkInvite implements Take {

    /**
     * Size of buffer.
     */
    private static final int BUFFER_SIZE = 8192;

    /**
     * RegEx to validate mail.
     */
    private static final Pattern MAIL_MASK = Pattern.compile(
        "[a-z_\\.\\-A-Z0-9]+[@][a-z_\\.\\-A-Z0-9]+"
    );

    /**
     * Base.
     */
    private final transient Base base;

    /**
     * Ctor.
     * @param bse Base
     */
    TkInvite(final Base bse) {
        this.base = bse;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final String invite = new RqForm.Smart(
            new RqForm.Base(req)
        ).single("name");
        final String guest;
        if (MAIL_MASK.matcher(invite).find()) {
            guest = this.inviteByEmail(invite);
        } else {
            guest = invite;
        }
        final String check = new RqAlias(this.base, req)
            .user().aliases().check(guest);
        if (check.isEmpty()) {
            throw new RsFailure(
                String.format("incorrect alias \"%s\", try again", guest)
            );
        }
        final Bout bout = new RqBout(this.base, req).bout();
        try {
            bout.friends().invite(guest);
        } catch (final Friends.UnknownAliasException ex) {
            throw new RsFailure(ex);
        }
        throw new RsForward(
            new RsFlash(
                String.format(
                    "\"%s\" invited to the bout #%d",
                    guest, bout.number()
                ),
                Level.INFO
            )
        );
    }

    /**
     * Invite a user to participate on NetBout by email.
     * @param invite Email.
     * @return Alias.
     * @throws IOException If fails.
     * @todo #602:30min/DEV Invited user should receive an email message with
     *  a text like this 'You are invited into the Netbout click on the link
     *  to register' and the link should be
     *  `http://www.netbout.com/b/<bout_number>?invite=<invite-key>`
     *  where `invite-key` is the encrypted urn. We can use `CcAES` from Takes
     *  project.
     */
    public String inviteByEmail(@NotNull(message = "Invite can't be NULL")
        final String invite) throws IOException {
        try {
            // @checkstyle MultipleStringLiteralsCheck (1 line)
            final String alias = invite.replace("@", "-").replace(".", "-");
            final String urn = String.format(
                "urn:email:%s",
                this.calcSha(alias)
            );
            this.base.user(URN.create(urn)).aliases().add(alias);
            new RqAlias(this.base, new RqWithAuth(urn)).alias().email(invite);
            return alias;
        } catch (final NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                String.format(
                    "It was not possible to invite \"%s\", try again",
                    invite
                ),
                ex
            );
        }
    }

    /**
     * Generate an insecure hash of the given message.
     * @param message Content to be hashed.
     * @return Hash
     * @throws NoSuchAlgorithmException If fails to get SHA1 algorithm.
     * @throws IOException If fails to hash.
     * @todo #602:30min/DEV All calcSha1 methods should be moved to a class
     *  that is responsible for hashing insecurely. And existent calls should
     *  be changed to use that new class. Remember that there is a unit test
     *  that should be moved to a new class.
     * @checkstyle ThrowsCountCheck (5 lines)
     */
    public String calcSha(@NotNull(message = "Message can't be NULL")
        final String message) throws IOException, NoSuchAlgorithmException {
        return this.calcSha(new ByteArrayInputStream(message.getBytes()));
    }

    /**
     * Generate an insecure hash of the given message.
     * @param stream Stream to be hashed.
     * @return Hash
     * @throws NoSuchAlgorithmException If fails to get SHA1 algorithm.
     * @throws IOException If fails to hash.
     * @checkstyle ThrowsCountCheck (15 lines)
     */
    public String calcSha(@NotNull(message = "Input Stream can't be NULL")
        final InputStream stream) throws IOException, NoSuchAlgorithmException {
        final byte[] buffer = new byte[BUFFER_SIZE];
        final MessageDigest digest = MessageDigest.getInstance("SHA1");
        final DigestInputStream dis = new DigestInputStream(
            new BufferedInputStream(stream),
            digest
        );
        try {
            // @checkstyle EmptyBlockCheck (2 lines)
            while (dis.read(buffer) != -1) {
            }
        } finally {
            dis.close();
        }
        return DatatypeConverter.printHexBinary(digest.digest());
    }
}

