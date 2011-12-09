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

import com.netbout.rest.page.PageBuilder;
import com.netbout.utils.Promoter;
import com.netbout.spi.Identity;
import com.netbout.spi.cpa.CpaHelper;
import java.net.URI;
import java.net.URL;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * Helper-related pages.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Path("/h")
public final class HelperRs extends AbstractRs {

    /**
     * Front page of the helper.
     * @return The JAX-RS response
     */
    @GET
    public Response front() {
        return new PageBuilder()
            .schema("")
            .stylesheet(this.base().path("/xsl/helper.xsl"))
            .build(AbstractPage.class)
            .init(this)
            .link("promote", this.base().path("/h/promote"))
            .authenticated(this.identity())
            .build();
    }

    /**
     * Promote this identity to helper.
     * @param addr The URL of the helper
     * @return The JAX-RS response
     */
    @POST
    @Path("/promote")
    public Response promote(@FormParam("url") final String addr) {
        URL url;
        try {
            url = new URL(addr);
        } catch (java.net.MalformedURLException ex) {
            throw new ForwardException(this, this.self(), ex);
        }
        final Identity identity = this.identity();
        new Promoter(this.hub()).promote(identity, url);
        return new PageBuilder()
            .build(AbstractPage.class)
            .init(this)
            .authenticated(identity)
            .status(Response.Status.SEE_OTHER)
            .location(this.self().build())
            .build();
    }

    /**
     * Get URI of front page.
     * @return The URI of front page
     */
    private UriBuilder self() {
        return this.base().path("/h");
    }

}
