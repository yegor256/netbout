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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.http.Request;
import com.jcabi.http.response.RestResponse;
import com.jcabi.http.response.XmlResponse;
import com.jcabi.log.Logger;
import com.netbout.spi.Attachment;
import com.netbout.spi.Attachments;
import java.io.IOException;
import java.net.HttpURLConnection;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * REST attachments.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.0
 */
@Immutable
@ToString(includeFieldNames = false)
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = "request")
final class RtAttachments implements Attachments {

    /**
     * Request to use.
     */
    private final transient Request request;

    /**
     * Public ctor.
     * @param req Request to use
     */
    RtAttachments(final Request req) {
        this.request = req;
    }

    @Override
    public int unseen() throws IOException {
        throw new UnsupportedOperationException("#unseen()");
    }

    @Override
    public void create(final String name) throws IOException {
        this.request.fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(XmlResponse.class)
            .rel("/page/links/link[@rel='create']/@href")
            .method(Request.POST)
            .body().formParam("name", name).back()
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_SEE_OTHER);
        Logger.info(this, "attachment '%s' created", name);
    }

    @Override
    public void delete(final String name) throws IOException {
        this.request.fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(XmlResponse.class)
            .rel(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "/page/bout/attachments/attachment[name='%s']/links/link[@rel='delete']/@href",
                    name
                )
            )
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_SEE_OTHER);
        Logger.info(this, "attachment '%s' deleted", name);
    }

    @Override
    public Attachment get(final String name) {
        return new RtAttachment(this.request, name);
    }

    @Override
    public Iterable<Attachment> iterate() throws IOException {
        return Iterables.transform(
            this.request.fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .as(XmlResponse.class)
                .xml()
                .xpath("/page/bout/attachments/attachment/name/text()"),
            new Function<String, Attachment>() {
                @Override
                public Attachment apply(final String name) {
                    return RtAttachments.this.get(name);
                }
            }
        );
    }
}
