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

import com.netbout.rest.jaxb.LongProfile;
import com.netbout.rest.page.JaxbBundle;
import com.netbout.rest.page.JaxbGroup;
import com.netbout.rest.page.PageBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * User profile.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Path("/pf")
public final class ProfileRs extends AbstractRs {

    /**
     * The profile page.
     * @return The JAX-RS response
     */
    @GET
    public Response front() {
        final LongProfile profile = new LongProfile(
            this.self(),
            this.identity()
        );
        return new PageBuilder()
            .stylesheet("/xsl/profile.xsl")
            .build(AbstractPage.class)
            .init(this)
            .append(profile)
            .render()
            .authenticated(this.identity())
            .build();
    }

    /**
     * Switch to another language.
     * @return The JAX-RS response
     */
    @GET
    @Path("/toggle")
    public Response toggle(@QueryParam("lang") final String code) {
        // todo
        return new PageBuilder()
            .build(AbstractPage.class)
            .init(this)
            .authenticated(this.identity())
            .entity(String.format("switched to '%s'", code))
            .status(Response.Status.SEE_OTHER)
            .location(this.self().build())
            .build();
    }

    /**
     * Location of myself.
     * @return The location, its builder actually
     */
    private UriBuilder self() {
        return this.base().path("/pf");
    }

}
