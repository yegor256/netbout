/**
 * Copyright (c) 2009-2014, Netbout.com
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
package com.netbout.rest;

import com.jcabi.urn.URN;
import com.netbout.client.RestSession;
import com.netbout.rest.jaxb.Invitee;
import com.netbout.rest.jaxb.LongBout;
import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import com.rexsl.page.CookieBuilder;
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.JaxbGroup;
import com.rexsl.page.Link;
import com.rexsl.page.PageBuilder;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.CookieParam;
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
 * @checkstyle ClassDataAbstractionCoupling (400 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
@Path("/{num : [0-9]+}")
public final class BoutRs extends BaseRs {

    /**
     * Threshold param.
     */
    public static final String PERIOD_PARAM = "p";

    /**
     * Place changing param.
     */
    public static final String PLACE_PARAM = "place";

    /**
     * Stage changing param.
     */
    public static final String STAGE_PARAM = "stage";

    /**
     * Number of the bout.
     */
    private transient Long number;

    /**
     * Query to filter messages with.
     */
    private transient String query = "";

    /**
     * Mask for suggestions of invitees.
     */
    private transient String mask;

    /**
     * Stage coordinates.
     */
    private transient StageCoordinates coords = new StageCoordinates();

    /**
     * The period we're looking at.
     */
    private transient String view = "";

    /**
     * Set number of bout.
     * @param num The number
     */
    @PathParam("num")
    public void setNumber(final Long num) {
        this.number = num;
    }

    /**
     * Set period to view.
     * @param name The name of it
     */
    @QueryParam(BoutRs.PERIOD_PARAM)
    public void setPeriod(final String name) {
        if (name != null) {
            this.view = name;
        }
    }

    /**
     * Set stage, if it's selected.
     * @param name The name of it
     */
    @QueryParam(BoutRs.STAGE_PARAM)
    public void setStage(final URN name) {
        if (name != null) {
            this.coords.setStage(name);
        }
    }

    /**
     * Set stage place.
     * @param place The place name
     */
    @QueryParam(BoutRs.PLACE_PARAM)
    public void setPlace(final String place) {
        if (place != null) {
            this.coords.setPlace(place);
        }
    }

    /**
     * Set filtering keyword.
     * @param keyword The query
     */
    @QueryParam(RestSession.QUERY_PARAM)
    public void setQuery(final String keyword) {
        if (keyword != null) {
            this.query = keyword;
        }
    }

    /**
     * Set suggestion keyword.
     * @param msk The mask
     */
    @QueryParam("mask")
    public void setMask(final String msk) {
        if (msk != null) {
            this.mask = msk;
        }
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
        final Response.ResponseBuilder resp =
            this.page().authenticated(this.identity());
        final String place = this.hub().make("post-render-change-place")
            .inBout(this.bout())
            .arg(this.bout().number())
            .arg(this.identity().name())
            .arg(this.coords.stage())
            .arg(this.coords.place())
            .noCache()
            .asDefault(this.coords.place())
            .exec();
        return resp.cookie(
            new CookieBuilder(this.self(""))
                .name("netbout-stage")
                .value(this.coords.copy().setPlace(place).toString())
                .temporary()
                .build()
        ).build();
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
        Message msg;
        try {
            msg = bout.post(text);
        } catch (Bout.MessagePostException ex) {
            throw new ForwardException(this, this.self(""), ex);
        }
        return new PageBuilder()
            .build(NbPage.class)
            .init(this)
            .authenticated(this.identity())
            .status(Response.Status.SEE_OTHER)
            .location(this.self("").build())
            .header("Message-number", msg.number())
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
            .build(NbPage.class)
            .init(this)
            .authenticated(this.identity())
            .status(Response.Status.SEE_OTHER)
            .location(this.self("").build())
            .build();
    }

    /**
     * Invite new person.
     * @param name Name of the invitee
     * @return The JAX-RS response
     */
    @Path("/i")
    @GET
    public Response invite(@QueryParam("name") final URN name) {
        final Bout bout = this.bout();
        if (name == null) {
            throw new ForwardException(
                this,
                this.self(""),
                "Query param 'name' missed"
            );
        }
        try {
            bout.invite(this.identity().friend(name));
        } catch (Identity.UnreachableURNException ex) {
            throw new ForwardException(this, this.self(""), ex);
        } catch (Bout.DuplicateInvitationException ex) {
            throw new ForwardException(this, this.self(""), ex);
        }
        return new PageBuilder()
            .build(NbPage.class)
            .init(this)
            .authenticated(this.identity())
            .status(Response.Status.SEE_OTHER)
            .location(this.self("").build())
            .header("Participant-name", name)
            .build();
    }

    /**
     * Confirm participation.
     * @return The JAX-RS response
     */
    @Path("/join")
    @GET
    public Response join() {
        this.bout().confirm();
        return new PageBuilder()
            .build(NbPage.class)
            .init(this)
            .authenticated(this.identity())
            .status(Response.Status.SEE_OTHER)
            .location(this.self("").build())
            .build();
    }

    /**
     * Leave this bout.
     * @return The JAX-RS response
     */
    @Path("/leave")
    @GET
    public Response leave() {
        this.bout().leave();
        return new PageBuilder()
            .build(NbPage.class)
            .init(this)
            .authenticated(this.identity())
            .status(Response.Status.SEE_OTHER)
            .location(this.base().build())
            .build();
    }

    /**
     * Kick-off somebody from the bout.
     * @param name Who to kick off
     * @return The JAX-RS response
     */
    @Path("/kickoff")
    @GET
    public Response kickoff(@QueryParam("name") final URN name) {
        Friend friend;
        try {
            friend = this.identity().friend(name);
        } catch (Identity.UnreachableURNException ex) {
            throw new ForwardException(this, this.base(), ex);
        }
        new Bout.Smart(this.bout()).participant(friend).kickOff();
        return new PageBuilder()
            .build(NbPage.class)
            .init(this)
            .authenticated(this.identity())
            .status(Response.Status.SEE_OTHER)
            .location(this.self("").build())
            .build();
    }

    /**
     * Stage dispatcher.
     * @return The stage RS resource
     */
    @Path("/s")
    public StageRs stageDispatcher() {
        final StageRs stage = new StageRs(this.bout(), this.coords);
        stage.setAuth(this.qauth());
        stage.setHttpHeaders(this.httpHeaders());
        stage.setHttpServletRequest(this.httpServletRequest());
        stage.setProviders(this.providers());
        stage.setSecurityContext(this.securityContext());
        stage.setServletContext(this.servletContext());
        stage.setUriInfo(this.uriInfo());
        return stage;
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
        } catch (Identity.BoutNotFoundException ex) {
            throw new ForwardException(this, this.base(), ex);
        }
        return bout;
    }

    /**
     * Main page.
     * @return The page
     */
    private NbPage page() {
        final Identity myself = this.identity();
        this.coords.normalize(this.hub(), this.bout());
        final NbPage page = new PageBuilder()
            .schema("")
            .stylesheet(
                this.base().path("/{bout}/xsl/{stage}/wrapper.xsl")
                    .build(this.bout().number(), this.coords.stage())
                    .toString()
            )
            .build(NbPage.class)
            .init(this)
            .searcheable(true)
            .append(
                new LongBout(
                    this.hub(),
                    this.bout(),
                    this.coords,
                    this.query,
                    this.self(""),
                    myself,
                    this.view
                )
            )
            .append(new JaxbBundle("query", this.query))
            .link(new Link("leave", "./leave"));
        this.appendInvitees(page);
        page.link(
            new Link(
                "top",
                this.self("").replaceQueryParam(
                    BoutRs.PERIOD_PARAM,
                    new Object[0]
                )
            )
        );
        if (new Bout.Smart(this.bout()).participant(myself).confirmed()) {
            page.link(new Link("post", "./p"));
        } else {
            page.link(new Link("join", "./join"));
        }
        if (new Bout.Smart(this.bout()).participant(myself).leader()) {
            page.link(new Link("rename", "./r"));
        }
        return page;
    }

    /**
     * Append invitees, if necessary.
     * @param page The page to append to
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void appendInvitees(final NbPage page) {
        if (this.mask != null) {
            final List<Invitee> invitees = new LinkedList<Invitee>();
            for (Friend friend : this.identity().friends(this.mask)) {
                invitees.add(new Invitee(friend, this.self("")));
            }
            page.append(new JaxbBundle("mask", this.mask))
                .append(JaxbGroup.build(invitees, "invitees"));
        }
    }

    /**
     * Location of myself.
     * @param path The path to add
     * @return The location, its builder actually
     */
    private UriBuilder self(final String path) {
        return UriBuilder.fromUri(
            this.base()
                .path("/{bout}")
                .path(path)
                .replaceQueryParam(RestSession.QUERY_PARAM, "{query}")
                .replaceQueryParam(BoutRs.PERIOD_PARAM, "{period}")
                .build(this.bout().number(), this.query, this.view)
        );
    }

}
