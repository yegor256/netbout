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
package com.netbout.rest;

import com.netbout.hub.User;
import com.netbout.rest.page.PageBuilder;
import com.netbout.spi.Identity;
import com.netbout.utils.Cryptor;
import java.io.IOException;
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
     * @param uname User name
     * @param iname Identity name
     * @param secret Secret word
     * @return The JAX-RS response
     */
    @GET
    public Response auth(@QueryParam("user") final String uname,
        @QueryParam("identity") final String iname,
        @QueryParam("secret") final String secret) {
        assert secret != null;
        Identity identity;
        try {
            identity = this.authenticate(uname, iname, secret);
        } catch (IOException ex) {
            throw new ForwardException(this, "/g", ex);
        }
        return new PageBuilder()
            .build(AbstractPage.class)
            .init(this)
            .authenticated(identity)
            .status(Response.Status.SEE_OTHER)
            .location(this.uriInfo().getBaseUri())
            .header("Netbout-auth", new Cryptor().encrypt(identity))
            .build();
    }

    /**
     * Authenticate the user through facebook.
     * @param uname User name
     * @param iname Identity name
     * @param secret Secret word
     * @return The identity found
     * @throws IOException If some problem with FB
     */
    private Identity authenticate(final String uname, final String iname,
        final String secret) throws IOException {
        assert secret != null;
        final User user = this.hub().user(uname);
        Identity identity;
        try {
            identity = user.identity(iname);
        } catch (com.netbout.spi.UnreachableIdentityException ex) {
            throw new IllegalStateException(
                String.format(
                    "Identity '%s' is not reachable: %s",
                    iname,
                    ex
                )
            );
        }
        // identity.alias("?");
        // identity.setPhoto(UriBuilder.fromUri("?").build().toURL());
        return identity;
    }

}
