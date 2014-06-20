/**
 * Copyright (c) 2009-2014, netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netbout Inc. located at www.netbout.com.
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

import com.google.common.net.HttpHeaders;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import com.jcabi.http.wire.AutoRedirectingWire;
import com.jcabi.http.wire.OneMinuteWire;
import com.jcabi.http.wire.RetryWire;
import com.netbout.spi.Friend;
import com.netbout.spi.Friends;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;

/**
 * RESTful front of a single friend.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 2.10
 */
@Path("/f")
public final class FriendRs extends BaseRs {

    /**
     * Get his photo.
     * @param bout Bout number
     * @param alias Friend's alias
     * @return The JAX-RS response
     * @throws IOException If fails
     */
    @GET
    @Path("/{bout: [0-9]+}/{alias: [a-zA-Z0-9]+}.png")
    public Response png(@PathParam("bout") final Long bout,
        @PathParam("alias") final String alias) throws IOException {
        final Friend friend = new Friends.Search(
            this.alias().inbox().bout(bout).friends()
        ).find(alias);
        final byte[] img = new JdkRequest(friend.photo())
            .through(RetryWire.class)
            .through(OneMinuteWire.class)
            .through(AutoRedirectingWire.class)
            .header(HttpHeaders.ACCEPT, "image/*")
            .header(HttpHeaders.USER_AGENT, "Netbout.com")
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .binary();
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(img));
        if (image == null) {
            image = ImageIO.read(new URL("http://img.netbout.com/unknown.png"));
        }
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        final CacheControl cache = new CacheControl();
        cache.setMaxAge((int) TimeUnit.DAYS.toSeconds(1L));
        cache.setPrivate(true);
        return Response.ok(new ByteArrayInputStream(baos.toByteArray()))
            .cacheControl(cache)
            .type("image/png")
            .build();
    }

}
