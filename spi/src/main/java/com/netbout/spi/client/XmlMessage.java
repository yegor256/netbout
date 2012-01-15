/**
 * Copyright (c) 2009-2011, NetBout.com
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
import java.util.Date;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

/**
 * The message, without direct connection to REST.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class XmlMessage implements Message {

    /**
     * Rest client.
     */
    private final transient RestClient client;

    /**
     * Rest response.
     */
    private final transient RestResponse response;

    /**
     * Number of the message.
     */
    private final transient Long num;

    /**
     * Public ctor.
     * @param clnt REST client
     * @param resp REST response
     * @param number Number of the message
     */
    public XmlMessage(final RestClient clnt, final RestResponse resp,
        final Long number) {
        this.client = clnt;
        this.response = resp;
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
        return new Friend(Urn.create(this.byPath("/author/text()")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String text() {
        return this.byPath("/text/text()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date date() {
        return ISODateTimeFormat
            .dateTime()
            .withZone(DateTimeZone.UTC)
            .parseDateTime(this.byPath("/text/text()"))
            .toDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean seen() {
        return Boolean.valueOf(this.byPath("/@seen"));
    }

    /**
     * Fetch by XPath.
     * @param path The path
     * @return The value found
     */
    public String byPath(final String path) {
        return this.response.xpath(
            String.format(
                "/page/bout/messages/message[number='%d']%s",
                this.num,
                path
            )
        ).get(0);
    }

}
