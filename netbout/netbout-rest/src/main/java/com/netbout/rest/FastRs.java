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
import com.netbout.spi.Urn;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Fast-lane URIs.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Path("/fast")
public final class FastRs extends AbstractRs {

    /**
     * Start a new bout with this first message and participants.
     * @param participants List of participants (comma separated)
     * @param message The message to post
     * @return The JAX-RS response
     */
    @GET
    @Path("/start")
    public Response start(@QueryParam("participants") final String participants,
        @QueryParam("message") final String message) {
        if (participants == null || message == null) {
            throw new ForwardException(
                this,
                this.base(),
                "Query params 'participants' and 'message' are mandatory"
            );
        }
        final Identity identity = this.identity();
        final Bout bout = identity.start();
        for (String dude : participants.split(",")) {
            try {
                bout.invite(identity.friend(Urn.create(dude)));
            } catch (com.netbout.spi.UnreachableUrnException ex) {
                throw new ForwardException(this, this.base(), ex);
            } catch (com.netbout.spi.DuplicateInvitationException ex) {
                throw new ForwardException(this, this.base(), ex);
            }
        }
        try {
            bout.post(message);
        } catch (com.netbout.spi.MessagePostException ex) {
            throw new ForwardException(this, this.base(), ex);
        }
        return new PageBuilder()
            .build(AbstractPage.class)
            .init(this)
            .authenticated(identity)
            .status(Response.Status.SEE_OTHER)
            .location(this.base().path("/{num}").build(bout.number()))
            .build();
    }

    /**
     * Start a new bout with this first message and participants.
     * @param participants List of participants (comma separated)
     * @param message The message to post
     * @return The JAX-RS response
     */
    @POST
    @Path("/start")
    public Response startPost(
        @FormParam("participants") final String participants,
        @FormParam("message") final String message) {
        return this.start(participants, message);
    }

}
