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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.http.Request;
import com.jcabi.http.response.RestResponse;
import com.jcabi.http.response.XmlResponse;
import com.jcabi.log.Logger;
import com.netbout.spi.Attachments;
import com.netbout.spi.Bout;
import com.netbout.spi.Friends;
import com.netbout.spi.Messages;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.util.Date;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * REST bout.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.0
 */
@Immutable
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = { "num", "request" })
@SuppressWarnings("PMD.TooManyMethods")
final class RtBout implements Bout {

    /**
     * Its number.
     */
    private final transient long num;

    /**
     * Request to use.
     */
    private final transient Request request;

    /**
     * Public ctor.
     * @param number The number
     * @param req Request to use
     */
    RtBout(final long number, final Request req) {
        this.num = number;
        this.request = req;
    }

    @Override
    public String toString() {
        return Long.toString(this.num);
    }

    @Override
    public long number() {
        return this.num;
    }

    @Override
    public Date date() throws IOException {
        try {
            return DateFormatUtils.ISO_DATETIME_FORMAT.parse(
                this.request.fetch()
                    .as(RestResponse.class)
                    .assertStatus(HttpURLConnection.HTTP_OK)
                    .as(XmlResponse.class)
                    .xml()
                    .xpath("/page/bout/date/text()")
                    .get(0)
            );
        } catch (final ParseException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Date updated() {
        throw new UnsupportedOperationException("#updated()");
    }

    @Override
    public String title() throws IOException {
        return this.request.fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(XmlResponse.class)
            .xml()
            .xpath("/page/bout/title/text()")
            .get(0);
    }

    @Override
    public void rename(final String text) throws IOException {
        this.request.fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(XmlResponse.class)
            .rel("/page/links/link[@rel='rename']/@href")
            .method(Request.POST)
            .body().formParam("title", text).back()
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_SEE_OTHER);
        Logger.info(this, "bout #%d renamed", this.num);
    }

    @Override
    public boolean subscription() throws IOException {
        return Boolean.valueOf(
            this.request.fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .as(XmlResponse.class)
                .xml()
                .xpath("/page/bout/subscription/text()")
                .get(0)
        );
    }

    @Override
    public void subscribe(final boolean subs) throws IOException {
        this.request.fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(XmlResponse.class)
            .rel("/page/links/link[@rel='subscribe']/@href")
            .method(Request.GET)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_SEE_OTHER);
        Logger.info(this, "bout #%d subscription changed", this.num);
    }

    @Override
    public Messages messages() {
        return new RtMessages(this.request);
    }

    @Override
    public Friends friends() {
        return new RtFriends(this.request);
    }

    @Override
    public Attachments attachments() {
        return new RtAttachments(this.request);
    }
}
