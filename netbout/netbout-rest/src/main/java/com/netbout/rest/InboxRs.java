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

import com.netbout.rest.jaxb.ShortBout;
import com.netbout.rest.page.JaxbBundle;
import com.netbout.rest.page.JaxbGroup;
import com.netbout.rest.page.PageBuilder;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * RESTful front of user's inbox.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Path("/")
public final class InboxRs extends AbstractRs {

    /**
     * Query to filter messages with.
     */
    private transient String query;

    /**
     * Set filtering keyword.
     * @param keyword The query
     */
    @QueryParam("q")
    public void setQuery(final String keyword) {
        this.query = keyword;
    }

    /**
     * Get inbox.
     * @param query Search query, if provided
     * @return The JAX-RS response
     */
    @GET
    public Response inbox() {
        final Identity identity = this.identity();
        final List<ShortBout> bouts = new ArrayList<ShortBout>();
        for (Bout bout : identity.inbox(this.query)) {
            bouts.add(
                ShortBout.build(
                    bout,
                    this.uriInfo().getBaseUriBuilder().clone()
                )
            );
        }
        return new PageBuilder()
            .stylesheet(
                this.uriInfo().getBaseUriBuilder()
                    .clone()
                    .path("/xsl/inbox.xsl")
                    .build()
                    .toString()
        )
            .build(AbstractPage.class)
            .init(this)
            .append(new JaxbBundle("query", this.query))
            .append(JaxbGroup.build(bouts, "bouts"))
            .authenticated(identity)
            .build();
    }

    /**
     * Start new bout.
     * @return The JAX-RS response
     */
    @Path("/s")
    @GET
    public Response start() {
        final Identity identity = this.identity();
        final Bout bout = identity.start();
        return new PageBuilder()
            .build(AbstractPage.class)
            .init(this)
            .authenticated(identity)
            .entity(String.format("bout #%d created", bout.number()))
            .status(Response.Status.SEE_OTHER)
            .location(
                this.uriInfo()
                    .getBaseUriBuilder()
                    .clone()
                    .path("/{num}")
                    .build(bout.number())
            )
            .build();
    }

}
