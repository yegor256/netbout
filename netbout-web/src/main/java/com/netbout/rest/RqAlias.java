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

import com.jcabi.urn.URN;
import com.netbout.spi.Alias;
import com.netbout.spi.Aliases;
import com.netbout.spi.Base;
import com.netbout.spi.User;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import lombok.EqualsAndHashCode;
import org.takes.Request;
import org.takes.facets.auth.Identity;
import org.takes.facets.auth.RqAuth;
import org.takes.facets.forward.RsFailure;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqWrap;

/**
 * User and alias retriever from request.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.14
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@EqualsAndHashCode(callSuper = true)
public final class RqAlias extends RqWrap {

    /**
     * Base.
     */
    private final transient Base base;

    /**
     * Ctor.
     * @param bse The base
     * @param req Request
     */
    public RqAlias(final Base bse, final Request req) {
        super(req);
        this.base = bse;
    }

    /**
     * Has alias?
     * @return TRUE if alias is there
     * @throws IOException If fails
     */
    public boolean has() throws IOException {
        final Identity identity = new RqAuth(this).identity();
        if (!identity.equals(Identity.ANONYMOUS)) {
            final Aliases aliases = this.user().aliases();
            if (!aliases.iterate().iterator().hasNext()
                && "urn:test:1".equals(identity.urn())) {
                aliases.add("tester");
            }
        }
        return !identity.equals(Identity.ANONYMOUS)
            && this.user().aliases().iterate().iterator().hasNext();
    }

    /**
     * Get user.
     * @return User
     * @throws IOException If fails
     */
    public User user() throws IOException {
        final Identity identity = new RqAuth(this).identity();
        if (identity.equals(Identity.ANONYMOUS)) {
            throw new RsFailure("you are not logged in yet");
        }
        return this.base.user(URN.create(identity.urn()));
    }

    /**
     * Get alias.
     * @return Alias
     * @throws IOException If fails
     */
    public Alias alias() throws IOException {
        final Aliases aliases = this.user().aliases();
        if (!aliases.iterate().iterator().hasNext()) {
            throw new RsForward(
                HttpURLConnection.HTTP_MOVED_TEMP,
                "/login/start"
            );
        }
        final Alias alias = aliases.iterate().iterator().next();
        final URI photo;
        final Identity identity = new RqAuth(this).identity();
        if (identity.urn().startsWith("urn:github:")) {
            photo = URI.create(identity.properties().get("avatar"));
        } else if (identity.urn().startsWith("urn:facebook:")
            || identity.urn().startsWith("urn:google:")) {
            photo = URI.create(identity.properties().get("picture"));
        } else {
            photo = Alias.BLANK;
        }
        if (!alias.photo().equals(photo)) {
            alias.photo(photo);
        }
        return alias;
    }
}
