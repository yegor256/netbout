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
import com.jcabi.http.response.RestResponse;
import com.jcabi.http.response.XmlResponse;
import com.jcabi.log.Logger;
import com.netbout.spi.Bout;
import com.netbout.spi.Inbox;
import com.netbout.spi.Pageable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * REST inbox.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.0
 */
@Immutable
@ToString
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = "request")
final class RtInbox implements Inbox {

    /**
     * Request to use.
     */
    private final transient Request request;

    /**
     * Public ctor.
     * @param req Request to use
     */
    RtInbox(final Request req) {
        this.request = req;
    }

    @Override
    public long start() throws IOException {
        final long number = Long.parseLong(
            this.request.fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .as(XmlResponse.class)
                .rel("/page/links/link[@rel='start']/@href")
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_SEE_OTHER)
                .follow()
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .as(XmlResponse.class)
                .xml()
                .xpath("/page/bout/number/text()")
                .get(0)
        );
        Logger.info(this, "bout #%d started", number);
        return number;
    }

    @Override
    public long unread() {
        throw new UnsupportedOperationException("#unread()");
    }

    @Override
    public Bout bout(final long number) {
        return new RtBout(
            number,
            this.request
                .uri().path("/b").path(Long.toString(number)).back()
        );
    }

    @Override
    public Pageable<Bout> jump(final long number) {
        throw new UnsupportedOperationException("#jump()");
    }

    @Override
    public Iterable<Bout> iterate() {
        return new Iterable<Bout>() {
            @Override
            public Iterator<Bout> iterator() {
                return new RtBoutIterator(RtInbox.this.request);
            }
        };
    }

    @Override
    public Iterable<Bout> search(final String term) throws IOException {
        final List<Bout> result = new ArrayList<>(16);
        for (final Bout bout : this.iterate()) {
            if (bout.title().contains(term)) {
                result.add(bout);
            } else if (bout.messages().search(term).iterator().hasNext()) {
                result.add(bout);
            }
        }
        return result;
    }
}
