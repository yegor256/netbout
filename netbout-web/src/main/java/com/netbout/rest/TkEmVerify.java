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
package com.netbout.rest;

import com.jcabi.manifests.Manifests;
import com.jcabi.urn.URN;
import com.netbout.spi.Alias;
import com.netbout.spi.Base;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.flash.RsFlash;
import org.takes.facets.forward.RsFailure;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqHref;

/**
 * Verifies email.
 *
 * @author Dragan Bozanovic (bozanovicdr@gmail.com)
 * @version $Id$
 * @since 2.21
 */
public final class TkEmVerify implements Take {

    /**
     * Encryptor.
     */
    private static final StandardPBEStringEncryptor ENC =
        new StandardPBEStringEncryptor();

    /**
     * Verification code pattern.
     */
    private static final Pattern PATTERN =
        Pattern.compile("(.+):([^:]+):([^:]+)");

    /**
     * Base.
     */
    private final transient Base base;

    static {
        TkEmVerify.ENC.setPassword(Manifests.read("Netbout-EmailCryptSecret"));
    }

    /**
     * Ctor.
     * @param bas Base
     */
    public TkEmVerify(final Base bas) {
        super();
        this.base = bas;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final String invalid = "verification link not valid";
        final String path = new RqHref.Base(req).href().toString();
        final Matcher matcher = TkEmVerify.PATTERN.matcher(
            TkEmVerify.ENC.decrypt(
                URLDecoder.decode(
                    path.substring(path.lastIndexOf('/') + 1),
                    "UTF-8"
                )
            )
        );
        if (!matcher.matches()) {
            throw new RsFailure(invalid);
        }
        final Alias alias = this.getAlias(matcher.group(1), matcher.group(2));
        if (alias == null) {
            throw new RsFailure(invalid);
        }
        final String current = alias.email();
        final char excl = '!';
        if (current.indexOf(excl) < 0) {
            throw new RsFailure("no email verification in progress");
        }
        final String newEmail = current.substring(current.indexOf(excl) + 1);
        // @checkstyle MagicNumber (1 line)
        if (!matcher.group(3).equals(newEmail)) {
            throw new RsFailure(invalid);
        }
        alias.email(newEmail);
        return new RsForward(new RsFlash("email verified"));
    }

    /**
     * Finds alias for the provided urn and name.
     * @param urn Urn
     * @param alias Alias name
     * @return Alias
     * @throws IOException If some problem inside
     */
    private Alias getAlias(final String urn, final String alias)
        throws IOException {
        Alias result = null;
        for (final Alias als
            : this.base.user(URN.create(urn)).aliases().iterate()) {
            if (als.name().equals(alias)) {
                result = als;
                break;
            }
        }
        return result;
    }
}
