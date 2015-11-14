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
package com.netbout.rest.account;

import com.jcabi.manifests.Manifests;
import com.netbout.rest.RqAlias;
import com.netbout.spi.Alias;
import com.netbout.spi.Base;
import java.io.IOException;
import java.net.URLEncoder;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.auth.RqAuth;
import org.takes.facets.flash.RsFlash;
import org.takes.facets.forward.RsFailure;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqForm;
import org.takes.rq.RqHref;

/**
 * User account.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.14
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @todo #738 If the email attribute value contains !,
 *  we should display an exclamation icon with hint that
 *  the email is not verified yet near the alias name.
 */
final class TkSaveEmail implements Take {

    /**
     * Encryptor.
     */
    private static final StandardPBEStringEncryptor ENC =
        new StandardPBEStringEncryptor();

    /**
     * Base.
     */
    private final transient Base base;

    static {
        TkSaveEmail.ENC.setPassword(
            Manifests.read("Netbout-EmailCryptSecret")
        );
    }

    /**
     * Ctor.
     * @param bse Base
     */
    TkSaveEmail(final Base bse) {
        this.base = bse;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final String email = new RqForm.Smart(
            new RqForm.Base(req)
        ).single("email");
        final Alias alias = new RqAlias(this.base, req).alias();
        final String code = URLEncoder.encode(
            TkSaveEmail.ENC.encrypt(
                String.format(
                    "%s:%s:%s",
                    new RqAuth(req).identity().urn(), alias.name(), email
                )
            ), "UTF-8"
        );
        final String link = String.format(
            "%semverify/%s",
            new RqHref.Smart(new RqHref.Base(req)).home().bare(), code
        );
        final String old;
        if (alias.email().contains("!")) {
            old = alias.email().substring(0, alias.email().indexOf('!'));
        } else {
            old = alias.email();
        }
        try {
            alias.email(String.format("%s!%s", old, email), link);
        } catch (final IOException ex) {
            throw new RsFailure(ex);
        }
        return new RsForward(
            new RsFlash(
                String.format(
                    // @checkstyle StringLiteralsConcatenationCheck (2 lines)
                    "Email changed to \"%s\". The verification "
                    + "link sent to this address.",
                    email
                )
            )
        );
    }
}
