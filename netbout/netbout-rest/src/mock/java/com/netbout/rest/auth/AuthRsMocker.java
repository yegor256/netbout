/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
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
package com.netbout.rest.auth;

import com.jcabi.urn.URN;
import com.netbout.rest.BaseRs;
import com.netbout.rest.ForwardException;
import com.netbout.rest.NbPage;
import com.netbout.spi.Identity;
import com.rexsl.page.PageBuilder;
import java.net.URL;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Mocks authentication mechanism.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Path("/mock-auth")
public final class AuthRsMocker extends BaseRs {

    /**
     * Authentication page.
     * @param iname Name of identity
     * @param secret The secret code
     * @return The JAX-RS response
     * @throws Exception If some problem
     */
    @GET
    public Response auth(@QueryParam("identity") final URN iname,
        @QueryParam("secret") final String secret) throws Exception {
        if ((iname == null) || (secret == null)) {
            throw new ForwardException(this, this.base(), "NULL inputs");
        }
        if (!"test".equals(iname.nid())) {
            throw new ForwardException(this, this.base(), "Invalid NID");
        }
        if (!secret.isEmpty()) {
            throw new ForwardException(this, this.base(), "Wrong secret");
        }
        final Identity identity = new ResolvedIdentity(
            this.base().path("/mock-auth").build().toURL(),
            iname
        );
        identity.profile().setPhoto(
            new URL("http://img.netbout.com/unknown.png")
        );
        identity.profile().alias(iname.nss());
        return new PageBuilder()
            .build(NbPage.class)
            .init(this)
            .render()
            .authenticated(identity)
            .build();
    }

}
