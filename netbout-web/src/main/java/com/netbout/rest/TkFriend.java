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

import com.google.common.collect.Iterables;
import com.google.common.net.HttpHeaders;
import com.jcabi.aspects.Tv;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import com.jcabi.http.wire.AutoRedirectingWire;
import com.jcabi.http.wire.OneMinuteWire;
import com.jcabi.http.wire.RetryWire;
import com.netbout.spi.Base;
import com.netbout.spi.Friend;
import com.netbout.spi.User;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rs.RsFluent;

/**
 * Friend.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.14
 */
public final class TkFriend implements TkRegex {

    /**
     * Base.
     */
    private final transient Base base;

    /**
     * Ctor.
     * @param bse Base
     */
    public TkFriend(final Base bse) {
        this.base = bse;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        final User user = new RqAlias(this.base, req).user();
        final String alias = req.matcher().group(1);
        final Iterable<Friend> opts = user.friends(alias);
        if (Iterables.isEmpty(opts)) {
            throw new RsFailure(
                String.format("alias '%s' not found", alias)
            );
        }
        final Friend friend = opts.iterator().next();
        if (!friend.alias().equals(alias)) {
            throw new RsFailure(
                String.format("alias '%s' is not found", alias)
            );
        }
        final byte[] img = new JdkRequest(friend.photo())
            .through(AutoRedirectingWire.class)
            .through(RetryWire.class)
            .through(OneMinuteWire.class)
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
        final Image thumb = image.getScaledInstance(
            Tv.HUNDRED, -1, Image.SCALE_SMOOTH
        );
        final BufferedImage bthumb = new BufferedImage(
            thumb.getWidth(null), thumb.getHeight(null),
            BufferedImage.TYPE_INT_RGB
        );
        bthumb.getGraphics().drawImage(thumb, 0, 0, null);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bthumb, "png", baos);
        return new RsFluent()
            .withType("image/png")
            .withHeader(
                "Cache-Control",
                String.format(
                    "private, max-age=%d",
                    TimeUnit.DAYS.toSeconds(1L)
                )
            )
            .withBody(new ByteArrayInputStream(baos.toByteArray()));
    }

}
