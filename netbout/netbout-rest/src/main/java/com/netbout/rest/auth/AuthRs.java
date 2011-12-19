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
package com.netbout.rest.auth;

import com.netbout.rest.AbstractPage;
import com.netbout.rest.AbstractRs;
import com.netbout.rest.LoginRequiredException;
import com.netbout.rest.page.PageBuilder;
import com.netbout.spi.Identity;
import com.netbout.spi.Urn;
import com.netbout.utils.Cryptor;
import com.ymock.util.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * REST authentication page.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Path("/auth")
public final class AuthRs extends AbstractRs {

    /**
     * Authentication page.
     * @param iname Identity name
     * @param secret Secret word
     * @return The JAX-RS response
     */
    @GET
    public Response auth(@QueryParam("identity") final Urn iname,
        @QueryParam("secret") final String secret) {
        if (iname == null || secret == null) {
            throw new LoginRequiredException(
                this,
                "'identity' and 'secret' query params are mandatory"
            );
        }
        this.logoff();
        final Identity identity = this.authenticate(iname, secret);
        return new PageBuilder()
            .build(AbstractPage.class)
            .init(this)
            .authenticated(identity)
            .status(Response.Status.SEE_OTHER)
            .location(this.base().build())
            .header("Netbout-auth", new Cryptor().encrypt(identity))
            .build();
    }

    /**
     * Authenticate the user through facebook.
     * @param iname Identity name
     * @param secret Secret word
     * @return The identity found
     */
    private Identity authenticate(final Urn iname,
        final String secret) {
        Identity remote;
        try {
            remote = new AuthMediator(this.hub().resolver())
                .authenticate(iname, secret);
        } catch (java.io.IOException ex) {
            Logger.warn(this, "%[exception]s", ex);
            throw new LoginRequiredException(this, ex);
        }
        Identity identity;
        try {
            identity = this.hub().identity(iname);
        } catch (com.netbout.spi.UnreachableUrnException ex) {
            throw new LoginRequiredException(this, ex);
        }
        for (String alias : remote.aliases()) {
            identity.alias(alias);
        }
        identity.setPhoto(remote.photo());
        return identity;
    }

}
