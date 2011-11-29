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

import com.netbout.hub.HubEntry;
import com.netbout.rest.jaxb.Invitee;
import com.netbout.rest.jaxb.LongBout;
import com.netbout.rest.page.JaxbBundle;
import com.netbout.rest.page.JaxbGroup;
import com.netbout.rest.page.PageBuilder;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Participant;
import com.rexsl.core.Manifests;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * RESTful front of one Bout.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
@Path("/{num : [0-9]+}")
public final class BoutRs extends AbstractRs {

    /**
     * Number of the bout.
     */
    private transient Long number;

    /**
     * Query to filter messages with.
     */
    private transient String query;

    /**
     * Stage coordinates.
     */
    private transient StageCoordinates coords = new StageCoordinates();

    /**
     * Set number of bout.
     * @param num The number
     */
    @PathParam("num")
    public void setNumber(final Long num) {
        this.number = num;
    }

    /**
     * Set stage, if it's selected.
     * @param name The name of it
     */
    @QueryParam("stage")
    public void setStage(final String name) {
        if (name != null) {
            this.coords.setStage(name);
        }
    }

    /**
     * Set stage place.
     * @param place The place name
     */
    @QueryParam("place")
    public void setPlace(final String place) {
        if (place != null) {
            this.coords.setPlace(place);
        }
    }

    /**
     * Set filtering keyword.
     * @param keyword The query
     */
    @QueryParam("q")
    public void setQuery(final String keyword) {
        this.query = keyword;
    }

    /**
     * Set stage coordinates.
     * @param cookie The information from cookie
     */
    @CookieParam("netbout-stage")
    public void setStageCoords(final String cookie) {
        this.coords = StageCoordinates.valueOf(cookie);
    }

    /**
     * Get bout front page.
     * @return The JAX-RS response
     */
    @GET
    public Response front() {
        return this.page()
            .authenticated(this.identity())
            .cookie(this.stageCookie())
            .build();
    }

    /**
     * Get bout front page, with suggestions for invites.
     * @param keyword The keyword to use
     * @return The JAX-RS response
     */
    @GET
    @Path("/s")
    public Response suggest(@QueryParam("k") final String keyword) {
        if (keyword == null) {
            throw new ForwardException(
                this,
                this.self(""),
                "Query param 'k' missed"
            );
        }
        final List<Invitee> invitees = new ArrayList<Invitee>();
        for (Identity identity : HubEntry.find(keyword)) {
            invitees.add(
                Invitee.build(
                    identity,
                    UriBuilder.fromUri(this.self(""))
                )
            );
        }
        return this.page()
            .append(new JaxbBundle("keyword", keyword))
            .append(JaxbGroup.build(invitees, "invitees"))
            .authenticated(this.identity())
            .cookie(this.stageCookie())
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
                this.self(""),
                "Form param 'text' missed"
            );
        }
        bout.post(text);
        return new PageBuilder()
            .build(AbstractPage.class)
            .init(this)
            .authenticated(this.identity())
            .status(Response.Status.SEE_OTHER)
            .location(this.self(""))
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
                this.self(""),
                "Form param 'title' missed"
            );
        }
        bout.rename(title);
        return new PageBuilder()
            .build(AbstractPage.class)
            .init(this)
            .authenticated(this.identity())
            .status(Response.Status.SEE_OTHER)
            .location(this.self(""))
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
                this.self(""),
                "Form param 'name' missed"
            );
        }
        bout.invite(this.identity().friend(name));
        return new PageBuilder()
            .build(AbstractPage.class)
            .init(this)
            .authenticated(this.identity())
            .status(Response.Status.SEE_OTHER)
            .location(this.self(""))
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
            .status(Response.Status.SEE_OTHER)
            .location(this.self(""))
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
            .status(Response.Status.SEE_OTHER)
            .location(this.self(""))
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
        this.coords.normalize(this.bout());
        final Page page = new PageBuilder()
            .stylesheet(
                UriBuilder.fromUri(this.self("/xsl/bout.xsl"))
                    .queryParam("stage", this.coords.stage())
                    .build()
                    .toString()
        )
            .build(AbstractPage.class)
            .init(this)
            .append(
                new LongBout(
                    this.bout(),
                    this.coords,
                    this.query,
                    UriBuilder.fromUri(this.self(""))
                )
            )
            .append(new JaxbBundle("query", this.query))
            .link("leave", this.self("/leave"));
        if (this.participant().confirmed()) {
            page.link("post", this.self("/p"))
                .link("invite", this.self("/i"))
                .link("suggest", this.self("/s"))
                .link("rename", this.self("/r"));
        } else {
            page.link("join", this.self("/join"));
        }
        return page;
    }

    /**
     * Location of myself.
     * @param path The path to add
     * @return The location
     */
    private URI self(final String path) {
        return this.uriInfo()
            .getBaseUriBuilder()
            .clone()
            .path("/{num}")
            .path(path)
            .build(this.bout().number());
    }

    /**
     * Create cookie for stage.
     * @return The cookie
     */
    private NewCookie stageCookie() {
        return new NewCookie(
            "netbout-stage",
            this.coords.toString(),
            this.self("").getPath(),
            this.uriInfo().getBaseUri().getHost(),
            Integer.valueOf(Manifests.read("Netbout-Revision")),
            "Netbout.com stage information",
            // @checkstyle MagicNumber (1 line)
            60 * 60 * 24 * 90,
            false
        );
    }

}
