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
package com.netbout.client;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.jcabi.aspects.Loggable;
import com.jcabi.http.Request;
import com.jcabi.http.response.RestResponse;
import com.jcabi.http.response.XmlResponse;
import com.jcabi.xml.XML;
import com.netbout.spi.Bout;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * REST bout iterator.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.14
 */
@ToString
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = { "request", "bouts" })
final class RtBoutIterator implements Iterator<Bout> {

    /**
     * Pre-fetched bouts.
     */
    private final transient Queue<Bout> bouts = new LinkedList<Bout>();

    /**
     * Request to use.
     */
    private transient Request request;

    /**
     * Has more?
     */
    private transient boolean more = true;

    /**
     * Public ctor.
     * @param req Request to use
     */
    RtBoutIterator(final Request req) {
        this.request = req;
    }

    @Override
    public boolean hasNext() {
        if (this.bouts.isEmpty() && this.more) {
            try {
                this.fetch();
            } catch (final IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return !this.bouts.isEmpty();
    }

    @Override
    public Bout next() {
        if (!this.hasNext()) {
            throw new NoSuchElementException("end of inbox");
        }
        return this.bouts.poll();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("#remove()");
    }

    /**
     * Fetch more.
     * @throws IOException If fails
     */
    private void fetch() throws IOException {
        final XmlResponse response = this.request.fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(XmlResponse.class);
        final XML xml = response.xml();
        this.bouts.addAll(
            Lists.transform(
                xml.xpath("/page/bouts/bout/number/text()"),
                new Function<String, Bout>() {
                    @Override
                    public Bout apply(final String input) {
                        final long number = Long.parseLong(input);
                        return new RtBout(
                            number,
                            RtBoutIterator.this.request
                                .uri().path("/b")
                                .path(Long.toString(number))
                                .back()
                        );
                    }
                }
            )
        );
        if (xml.nodes("/page/bouts/bout").isEmpty()) {
            this.more = false;
        } else {
            this.request = response.rel(
                "/page/bouts/bout[last()]/links/link[@rel='more']/@href"
            );
        }
    }

}
