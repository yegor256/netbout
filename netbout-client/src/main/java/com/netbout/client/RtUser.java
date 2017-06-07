/**
 * Copyright (c) 2009-2017, netbout.com
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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.http.Request;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.wire.CookieOptimizingWire;
import com.netbout.spi.Aliases;
import com.netbout.spi.Friend;
import com.netbout.spi.User;
import java.net.URI;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * RESTful Netbout user.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 2.0
 */
@Immutable
@ToString(includeFieldNames = false)
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = "request")
public final class RtUser implements User {

    /**
     * Request to use.
     */
    private final transient Request request;

    /**
     * Public ctor.
     * @param token Authentication token
     */
    public RtUser(@NotNull final String token) {
        this(URI.create("http://www.netbout.com"), token);
    }

    /**
     * Public ctor.
     * @param uri Home page URI
     * @param token Authentication token
     */
    public RtUser(@NotNull final URI uri, @NotNull final String token) {
        this.request = new JdkRequest(uri)
            .through(CookieOptimizingWire.class)
            .header(HttpHeaders.COOKIE, String.format("PsCookie=%s", token))
            .header(HttpHeaders.ACCEPT, MediaType.TEXT_XML);
    }

    @Override
    public Aliases aliases() {
        return new RtAliases(this.request);
    }

    @Override
    public Iterable<Friend> friends(
        @NotNull(message = "text can't be NULL") final String text) {
        throw new UnsupportedOperationException("#friends()");
    }
}
