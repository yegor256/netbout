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
package com.netbout.client;

import com.jcabi.aspects.Immutable;
import com.jcabi.http.Request;
import com.jcabi.http.response.RestResponse;
import com.jcabi.http.response.XmlResponse;
import com.netbout.spi.Alias;
import com.netbout.spi.Bout;
import com.netbout.spi.Pageable;
import java.io.IOException;
import java.net.URI;

/**
 * REST aliases.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 2.0
 */
@Immutable
final class RtAlias implements Alias {

    /**
     * Request to use.
     */
    private final transient Request request;

    /**
     * Public ctor.
     * @param req Request to use
     */
    RtAlias(final Request req) {
        this.request = req;
    }

    @Override
    public String name() throws IOException {
        return this.request.fetch()
            .as(XmlResponse.class)
            .xml()
            .xpath("/page/alias/name/text()")
            .get(0);
    }

    @Override
    public URI photo() throws IOException {
        return URI.create(
            this.request.fetch()
                .as(XmlResponse.class)
                .xml()
                .xpath("/page/alias/photo/text()")
                .get(0)
        );
    }

    @Override
    public void photo(final String uri) {
        throw new UnsupportedOperationException(
            "#photo(): it is not possible to change photo through API"
        );
    }

    @Override
    public long start() throws IOException {
        return Long.parseLong(
            this.request.fetch()
                .as(XmlResponse.class)
                .rel("/page/links/link[@rel='start']/@href")
                .fetch()
                .as(RestResponse.class)
                .follow()
                .fetch()
                .as(XmlResponse.class)
                .xml()
                .xpath("/page/bout/number/text()")
                .get(0)
        );
    }

    @Override
    public Pageable<Bout> inbox() {
        throw new UnsupportedOperationException(
            "#inbox(): not possible to list bouts at the moment"
        );
    }

    @Override
    public Bout bout(final long number) {
        return new RtBout(
            this.request
                .uri()
                .set(this.request.uri().path(Long.toString(number)).get())
                .back()
        );
    }
}
