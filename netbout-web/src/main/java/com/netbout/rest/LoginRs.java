/**
 * Copyright (c) 2009-2014, netbout.com
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

import com.rexsl.page.Link;
import com.rexsl.page.PageBuilder;
import com.rexsl.page.auth.Identity;
import com.rexsl.page.inset.FlashInset;
import java.io.IOException;
import java.util.logging.Level;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * RESTful front of login functions.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@Path("/login")
public final class LoginRs extends BaseRs {

    /**
     * Login page.
     * @return The JAX-RS response
     */
    @GET
    @Path("/")
    public Response login() {
        if (!this.auth().identity().equals(Identity.ANONYMOUS)) {
            throw FlashInset.forward(
                this.uriInfo().getBaseUri(),
                "you are logged in already",
                Level.INFO
            );
        }
        return new PageBuilder()
            .stylesheet("/xsl/login.xsl")
            .build(NbPage.class)
            .init(this)
            .render()
            .build();
    }

    /**
     * Register page.
     * @return The JAX-RS response
     */
    @GET
    @Path("/r")
    public Response start() {
        this.user();
        return new PageBuilder()
            .stylesheet("/xsl/register.xsl")
            .build(NbPage.class)
            .init(this)
            .link(new Link("register", "."))
            .link(new Link("check", "./check"))
            .render()
            .build();
    }

    /**
     * Register and continue.
     * @param alias Alias to try
     */
    @POST
    @Path("/r")
    public void register(@FormParam("alias") final String alias) {
        this.user().aliases().add(alias);
        throw FlashInset.forward(
            this.uriInfo().getBaseUri(),
            "your alias was registered",
            Level.INFO
        );
    }

    /**
     * Check availability.
     * @param alias Alias to check
     * @return Text "available" if this alias is available
     * @throws IOException If fails
     */
    @GET
    @Path("/r/check")
    public String check(@QueryParam("alias") final String alias)
        throws IOException {
        return this.user().aliases().check(alias);
    }

}
