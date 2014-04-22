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

import com.netbout.spi.Alias;
import com.netbout.spi.Bout;
import com.rexsl.page.Link;
import com.rexsl.page.PageBuilder;
import com.rexsl.page.inset.FlashInset;
import java.util.logging.Level;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * RESTful front of one Bout.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (400 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
@Path("/{num : [0-9]+}")
public final class BoutRs extends BaseRs {

    /**
     * Number of the bout.
     */
    private transient Long number;

    /**
     * Set number of bout.
     * @param num The number
     */
    @PathParam("num")
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
            .stylesheet("/xsl/bout.xsl")
            .build(NbPage.class)
            .init(this)
            .link(new Link("post", "./post"))
            .link(new Link("rename", "./rename"))
            .link(new Link("invite", "./invite"))
            .link(new Link("leave", "./leave"))
            .link(new Link("kick", "./kick"))
            .render()
            .build();
    }

    /**
     * Post new message to the bout.
     * @param text Text of message just posted
     */
    @POST
    @Path("/post")
    public void post(@FormParam("text") final String text) {
        this.bout().messages().post(text);
        throw FlashInset.forward(
            this.uriInfo().getBaseUri(),
            "message posted to the bout",
            Level.INFO
        );
    }

    /**
     * Rename this bout.
     * @param title New title to set
     */
    @POST
    @Path("/rename")
    public void rename(@FormParam("title") final String title) {
        this.bout().rename(title);
        throw FlashInset.forward(
            this.uriInfo().getBaseUri(),
            "bout renamed",
            Level.INFO
        );
    }

    /**
     * Invite new person.
     * @param name Name of the invitee
     */
    @GET
    @Path("/invite")
    public void invite(@QueryParam("name") final String name) {
        this.bout().friends().invite(name);
        throw FlashInset.forward(
            this.uriInfo().getBaseUri(),
            "new person invited to the bout",
            Level.INFO
        );
    }

    /**
     * Leave this bout.
     */
    @GET
    @Path("/leave")
    public void leave() {
        this.bout().friends().leave();
        throw FlashInset.forward(
            this.uriInfo().getBaseUri(),
            "you left this bout",
            Level.INFO
        );
    }

    /**
     * Kick-off somebody from the bout.
     * @param name Who to kick off
     */
    @GET
    @Path("/kick")
    public void kick(@QueryParam("name") final String name) {
        this.bout().friends().kick(name);
        throw FlashInset.forward(
            this.uriInfo().getBaseUri(),
            "you left this bout",
            Level.INFO
        );
    }

    /**
     * Get bout.
     * @return The bout
     */
    private Bout bout() {
        try {
            return this.alias().bout(this.number);
        } catch (final Alias.BoutNotFoundException ex) {
            throw FlashInset.forward(
                this.uriInfo().getBaseUri(),
                ex.getLocalizedMessage(),
                Level.SEVERE
            );
        }
    }

}
