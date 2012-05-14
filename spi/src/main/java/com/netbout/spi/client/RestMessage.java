/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi.client;

import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import com.netbout.spi.Urn;
import java.net.HttpURLConnection;
import java.util.Date;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

/**
 * The message.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
final class RestMessage implements Message {

    /**
     * Rest client.
     */
    private final transient RestClient client;

    /**
     * Number of the message.
     */
    private final transient Long num;

    /**
     * Public ctor.
     * @param clnt Rest client
     * @param number Number of the message
     */
    public RestMessage(final RestClient clnt, final Long number) {
        this.client = clnt;
        this.num = number;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Message msg) {
        return this.date().compareTo(msg.date());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object bout) {
        return bout == this || (bout instanceof Message
            && this.number().equals(((Message) bout).number()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.number().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("RestMessage#%d", this.number());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout bout() {
        return new RestBout(this.client.copy());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long number() {
        return this.num;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity author() {
        return new Friend(Urn.create(this.bySuffix("/author/text()")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String text() {
        return this.bySuffix("/text/text()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date date() {
        return ISODateTimeFormat
            .dateTime()
            .withZone(DateTimeZone.UTC)
            .parseDateTime(this.bySuffix("/date/text()"))
            .toDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean seen() {
        return Boolean.valueOf(this.bySuffix("/@seen"));
    }

    /**
     * Fetch by XPath suffix.
     * @param suffix The suffix of XPath
     * @return The value found
     */
    public String bySuffix(final String suffix) {
        return this.client
            .queryParam(
                RestSession.QUERY_PARAM,
                String.format("(equal $number %d)", this.num)
        )
            .get(String.format("reading %s of a message", suffix))
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath(
                String.format(
                    "/page/bout/messages/message[number='%d']",
                    this.num
                )
            )
            .xpath(
                String.format(
                    "/page/bout/messages/message[number='%d']%s",
                    this.num,
                    suffix
                )
            )
            .get(0);
    }

}
