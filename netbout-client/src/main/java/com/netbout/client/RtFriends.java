/**
 * Copyright (c) 2009-2015, netbout.com
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
package com.netbout.client;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.http.Request;
import com.jcabi.http.response.RestResponse;
import com.jcabi.http.response.XmlResponse;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.netbout.spi.Friend;
import com.netbout.spi.Friends;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * REST friends.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.0
 */
@Immutable
@ToString(includeFieldNames = false)
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = "request")
final class RtFriends implements Friends {
    /**
     * RsFlash cookie name.
     */
    private static final String COOKIE_RS_FLASH = "RsFlash";

    /**
     * Request to use.
     */
    private final transient Request request;

    /**
     * Public ctor.
     * @param req Request to use
     */
    RtFriends(final Request req) {
        this.request = req;
    }

    @Override
    public void invite(final String friend) throws IOException {
        final RestResponse response = this.request
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(XmlResponse.class)
            .rel("/page/links/link[@rel='invite']/@href")
            .method(Request.POST)
            // @checkstyle MultipleStringLiteralsCheck (1 line)
            .body().formParam("name", friend).back()
            .fetch()
            .as(RestResponse.class);
        if (response.status() == HttpURLConnection.HTTP_MOVED_PERM
            && response.cookie(RtFriends.COOKIE_RS_FLASH).getValue()
                .startsWith("incorrect+alias")
            ) {
            throw new Friends.UnknownAliasException(
                response.cookie(RtFriends.COOKIE_RS_FLASH).getValue()
            );
        }
        response.assertStatus(HttpURLConnection.HTTP_SEE_OTHER);
        Logger.info(this, "friend '%s' invited", friend);
    }

    @Override
    public void kick(final String friend) throws IOException {
        this.request
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(XmlResponse.class)
            .rel(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "/page/bout/friends/friend[alias='%s']/links/link[@rel='kick']/@href",
                    friend
                )
            )
            .uri().queryParam("name", friend).back()
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_SEE_OTHER);
        Logger.info(this, "friend '%s' kicked out", friend);
    }

    @Override
    public Iterable<Friend> iterate() throws IOException {
        return Iterables.transform(
            this.request
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .as(XmlResponse.class)
                .xml().nodes("/page/bout/friends/friend"),
            // @checkstyle AnonInnerLengthCheck (50 lines)
            new Function<XML, Friend>() {
                @Override
                public Friend apply(final XML xml) {
                    return new Friend() {
                        @Override
                        public String alias() {
                            return xml.xpath("alias/text()").get(0);
                        }
                        @Override
                        public URI photo() {
                            return URI.create(
                                xml.xpath(
                                    "links/link[@rel='photo']/@href"
                                ).get(0)
                            );
                        }
                        @Override
                        public String email() {
                            throw new UnsupportedOperationException("#email()");
                        }
                    };
                }
            }
        );
    }
}
