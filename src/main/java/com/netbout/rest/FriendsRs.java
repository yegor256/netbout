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

import com.jcabi.log.Logger;
import com.jcabi.urn.URN;
import com.netbout.rest.jaxb.Invitee;
import com.netbout.spi.Friend;
import com.netbout.spi.Identity;
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.JaxbGroup;
import com.rexsl.page.PageBuilder;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
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
        for (Friend friend : this.identity().friends(mask)) {
            invitees.add(
                new Invitee(
                    friend,
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
            .authenticated(this.identity())
            .build();
    }

    /**
     * Get photo of a friend.
     * @param urn URN of a friend
     * @return The JAX-RS response with a picture inside
     * @throws Exception If anything is wrong
     * @todo #158 Path annotation: http://java.net/jira/browse/JERSEY-739
     */
    @GET
    @Path("/photo")
    public Response photo(@QueryParam("urn") final URN urn) throws Exception {
        if (urn == null) {
            throw new ForwardException(
                this,
                this.base(),
                "Query param 'urn' is mandatory"
            );
        }
        final Identity self = this.identity();
        Friend friend;
        if (self.name().equals(urn)) {
            friend = self;
        } else {
            friend = self.friend(urn);
        }
        Response response = this.fetch(friend.profile().photo());
        if (response.getStatus() == HttpURLConnection.HTTP_NOT_FOUND) {
            response = this.fetch(
                new URL("http://img.netbout.com/unknown.png")
            );
        }
        return response;
    }

    /**
     * Fetch foto from the URL.
     * @param url The URL to fetch from
     * @return The JAX-RS response with a picture inside
     * @throws Exception If anything is wrong
     */
    private Response fetch(final URL url) throws Exception {
        final Response.ResponseBuilder builder = Response.ok();
        final HttpURLConnection conn =
            HttpURLConnection.class.cast(url.openConnection());
        final List<String> ifmodified = this.httpHeaders()
            .getRequestHeader(HttpHeaders.IF_MODIFIED_SINCE);
        if (ifmodified != null && !ifmodified.isEmpty()) {
            conn.setRequestProperty(
                HttpHeaders.IF_MODIFIED_SINCE,
                ifmodified.get(0)
            );
        }
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK
            || conn.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
            builder.status(conn.getResponseCode());
            final String[] headers = new String[] {
                HttpHeaders.CACHE_CONTROL,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.CONTENT_LENGTH,
                HttpHeaders.ETAG,
                HttpHeaders.EXPIRES,
                HttpHeaders.LAST_MODIFIED,
            };
            for (String header : headers) {
                final String value = conn.getHeaderField(header);
                if (value != null) {
                    builder.header(header, value);
                }
            }
            builder.header("X-Netbout-Original", url.toString());
            builder.entity(conn.getInputStream());
            Logger.debug(this, "#fetch('%s'): fetched", url);
        } else {
            builder.status(HttpURLConnection.HTTP_NOT_FOUND);
            Logger.warn(
                this,
                "#fetch('%s'): failed to fetch from (code=%d)",
                url,
                conn.getResponseCode()
            );
        }
        return builder.build();
    }

}
