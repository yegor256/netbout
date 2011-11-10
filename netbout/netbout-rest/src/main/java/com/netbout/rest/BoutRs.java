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
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import java.net.URI;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * RESTful front of one Bout.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Path("/{num : [0-9]+}")
public final class BoutRs extends AbstractRs {

    /**
     * Number of the bout.
     */
    @PathParam("num")
    private Long number;

    /**
     * Set bout number.
     * @param num The number
     */
    public void setNumber(final Long num) {
        this.number = num;
    }

    /**
     * Get bout front page.
     * @return The JAX-RS response
     */
    @GET
    public Response front() {
        return new PageBuilder()
            .stylesheet("bout")
            .build(AbstractPage.class)
            .init(this)
            .link("post", String.format("/%d/p", this.number))
            .link("invite", String.format("/%d/i", this.number))
            .append(this.bout())
            .authenticated(this.identity())
            .build();
    }

    /**
     * Post new message to the bout.
     * @param text Text of message just posted
     * @return The JAX-RS response
     */
    @Path("/p")
    @POST
    public Response post(@FormParam("text") final String text) {
        final Bout bout = this.bout();
        bout.post(text);
        return new PageBuilder()
            .build(AbstractPage.class)
            .init(this)
            .authenticated(this.identity())
            .entity("")
            .status(Response.Status.MOVED_PERMANENTLY)
            .location(this.self(bout))
            .build();
    }

    /**
     * Invite new person.
     * @param name Name of the invitee
     * @return The JAX-RS response
     */
    @Path("/i")
    @POST
    public Response invite(@FormParam("name") final String name) {
        final Bout bout = this.bout();
        Identity friend;
        try {
            friend = this.entry().identity(name);
        } catch (com.netbout.spi.UnknownIdentityException ex) {
            throw new ForwardException(
                this,
                this.self(bout),
                String.format(
                    "Invitee '%s' not found",
                    name
                )
            );
        }
        bout.invite(friend);
        return new PageBuilder()
            .build(AbstractPage.class)
            .init(this)
            .authenticated(this.identity())
            .entity("")
            .status(Response.Status.MOVED_PERMANENTLY)
            .location(this.self(bout))
            .build();
    }

    /**
     * Get bout.
     * @return The bout
     */
    private Bout bout() {
        final Identity identity = this.identity();
        Bout bout;
        try {
            bout = identity.bout(this.number);
        } catch (com.netbout.spi.BoutNotFoundException ex) {
            // @checkstyle MultipleStringLiterals (1 line)
            throw new ForwardException(this, "/", ex);
        }
        return bout;
    }

    /**
     * Location of myself.
     * @param bout The bout
     * @return The location
     */
    private URI self(final Bout bout) {
        return this.uriInfo()
            .getAbsolutePathBuilder()
            .replacePath("/{num}")
            .build(bout.number());
    }

}
