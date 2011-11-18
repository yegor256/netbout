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

import com.netbout.rest.jaxb.Invitee;
import com.netbout.rest.page.JaxbGroup;
import com.netbout.rest.page.PageBuilder;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Participant;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
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
        return this.page()
            .authenticated(this.identity())
            .build();
    }

    /**
     * Get bout front page, with suggestions for invites.
     * @param keyword The keyword to use
     * @return The JAX-RS response
     */
    @GET
    @Path("/s")
    public Response suggest(@QueryParam("q") final String keyword) {
        if (keyword == null) {
            throw new ForwardException(
                this,
                this.self(),
                "Query param 'q' missed"
            );
        }
        final List<Invitee> invitees = new ArrayList<Invitee>();
        for (Identity identity : this.entry().find(keyword)) {
            invitees.add(
                new Invitee(
                    identity,
                    UriBuilder.fromUri(this.self())
                )
            );
        }
        return this.page()
            .append(JaxbGroup.build(invitees, "invitees"))
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
        if (text == null) {
            throw new ForwardException(
                this,
                this.self(bout),
                "Form param 'text' missed"
            );
        }
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
     * Rename this bout.
     * @param title New title to set
     * @return The JAX-RS response
     */
    @Path("/r")
    @POST
    public Response rename(@FormParam("title") final String title) {
        final Bout bout = this.bout();
        if (title == null) {
            throw new ForwardException(
                this,
                this.self(bout),
                "Form param 'title' missed"
            );
        }
        bout.rename(title);
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
    @GET
    public Response invite(@QueryParam("name") final String name) {
        final Bout bout = this.bout();
        if (name == null) {
            throw new ForwardException(
                this,
                this.self(bout),
                "Form param 'name' missed"
            );
        }
        bout.invite(this.entry().identity(name));
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
     * Confirm participation.
     * @return The JAX-RS response
     */
    @Path("/join")
    @GET
    public Response join() {
        this.participant().confirm(true);
        return new PageBuilder()
            .build(AbstractPage.class)
            .init(this)
            .authenticated(this.identity())
            .entity("")
            .status(Response.Status.MOVED_PERMANENTLY)
            .location(this.self())
            .build();
    }

    /**
     * Leave this bout.
     * @return The JAX-RS response
     */
    @Path("/leave")
    @GET
    public Response leave() {
        this.participant().confirm(false);
        return new PageBuilder()
            .build(AbstractPage.class)
            .init(this)
            .authenticated(this.identity())
            .entity("")
            .status(Response.Status.MOVED_PERMANENTLY)
            .location(this.self())
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
     * Get me as a participant.
     * @return The participant
     */
    private Participant participant() {
        for (Participant participant : this.bout().participants()) {
            if (participant.identity().equals(this.identity())) {
                return participant;
            }
        }
        throw new IllegalStateException("Can't find myself in the bout");
    }

    /**
     * Main page.
     * @return The page
     */
    private Page page() {
        final Page page = new PageBuilder()
            .stylesheet("bout")
            .build(AbstractPage.class)
            .init(this)
            .append(this.bout())
            .link("leave", String.format("/%d/leave", this.number));
        if (this.participant().confirmed()) {
            page.link("post", String.format("/%d/p", this.number))
                .link("invite", String.format("/%d/i", this.number))
                .link("suggest", String.format("/%d/s", this.number))
                .link("rename", String.format("/%d/r", this.number));
        } else {
            page.link("join", String.format("/%d/join", this.number));
        }
        return page;
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

    /**
     * Location of myself.
     * @return The location
     */
    private URI self() {
        return this.self(this.bout());
    }

}
