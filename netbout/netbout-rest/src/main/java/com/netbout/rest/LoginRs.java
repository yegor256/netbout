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

import com.netbout.rest.page.PageBuilder;
import com.rexsl.core.Manifests;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * RESTful front of login functions.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Path("/g")
public final class LoginRs extends AbstractRs {

    /**
     * Login page.
     * @return The JAX-RS response
     * @see <a href="http://developers.facebook.com/docs/authentication/">facebook.com</a>
     */
    @GET
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public Response login() {
        final UriBuilder fburi = UriBuilder.fromUri(
            UriBuilder
                .fromPath("https://www.facebook.com/dialog/oauth")
                .replaceQueryParam("client_id", "{id}")
                .replaceQueryParam("redirect_uri", "{uri}")
                .build(
                    Manifests.read("Netbout-FbId"),
                    this.base().path("/fb/back").build()
                )
        );
        return new PageBuilder()
            .stylesheet("/xsl/login.xsl")
            .build(AbstractPage.class)
            .init(this, false)
            .link("facebook", fburi)
            .render()
            .preserved()
            .build();
    }

    /**
     * Login page for those who are already logged in, but want to upgrade
     * identity (or just to change it).
     * @return The JAX-RS response
     */
    @GET
    @Path("/re")
    public Response relogin() {
        return this.login();
    }

    /**
     * Logout page.
     * @return The JAX-RS response
     */
    @Path("/out")
    @GET
    public Response logout() {
        return new PageBuilder()
            .build(AbstractPage.class)
            .init(this, false)
            .anonymous()
            .status(Response.Status.SEE_OTHER)
            .location(this.base().build())
            .build();
    }

}
