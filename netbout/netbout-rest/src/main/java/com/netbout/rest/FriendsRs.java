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

import com.netbout.rest.jaxb.Invitee;
import com.netbout.spi.Identity;
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.JaxbGroup;
import com.rexsl.page.PageBuilder;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * Friends finding service (used by RESTful client or AJAX).
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Path("/f")
public final class FriendsRs extends BaseRs {

    /**
     * Get list of friends.
     * @param mask The mask
     * @param bout The bout number where you're going to use this list
     * @return The JAX-RS response
     * @todo #158 Path annotation: http://java.net/jira/browse/JERSEY-739
     */
    @GET
    @Path("/")
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Response list(@QueryParam("mask") final String mask,
        @QueryParam("bout") final String bout) {
        if (mask == null || bout == null) {
            throw new ForwardException(
                this,
                this.base(),
                "Query param 'mask' and 'bout' are mandatory"
            );
        }
        final List<Invitee> invitees = new LinkedList<Invitee>();
        for (Identity identity : this.identity().friends(mask)) {
            invitees.add(
                new Invitee(
                    identity,
                    UriBuilder.fromUri(this.base().path("/{bout}").build(bout))
                )
            );
        }
        return new PageBuilder()
            .schema("")
            .build(NbPage.class)
            .init(this)
            .append(new JaxbBundle("mask", mask))
            .append(JaxbGroup.build(invitees, "invitees"))
            .render()
            .authenticated(this.identity())
            .build();
    }

}
