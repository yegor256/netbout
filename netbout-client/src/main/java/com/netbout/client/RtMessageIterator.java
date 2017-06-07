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
import com.google.common.collect.Lists;
import com.jcabi.aspects.Loggable;
import com.jcabi.http.Request;
import com.jcabi.http.response.RestResponse;
import com.jcabi.http.response.XmlResponse;
import com.jcabi.xml.XML;
import com.netbout.spi.Message;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * REST bout iterator.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 2.14
 */
@ToString
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = { "request", "messages" })
final class RtMessageIterator implements Iterator<Message> {

    /**
     * Pre-fetched messages.
     */
    private final transient Queue<Message> messages;

    /**
     * Request to use.
     */
    private transient Request request;

    /**
     * Has more?
     */
    private transient boolean more;

    /**
     * Public ctor.
     * @param req Request to use
     */
    RtMessageIterator(final Request req) {
        this.request = req;
        this.messages = new LinkedList<Message>();
        this.more = true;
    }

    @Override
    public boolean hasNext() {
        if (this.messages.isEmpty() && this.more) {
            try {
                this.fetch();
            } catch (final IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return !this.messages.isEmpty();
    }

    @Override
    public Message next() {
        if (!this.hasNext()) {
            throw new NoSuchElementException("end of the bout");
        }
        return this.messages.poll();
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
        this.messages.addAll(
            Lists.transform(
                xml.nodes("/page/bout/messages/message"),
                new Function<XML, Message>() {
                    @Override
                    public Message apply(final XML node) {
                        return RtMessageIterator.msg(node);
                    }
                }
            )
        );
        if (xml.nodes("/page/bout/messages/message ").isEmpty()) {
            this.more = false;
        } else {
            this.request = response.rel(
                // @checkstyle LineLength (1 line)
                "/page/bout/messages/message[last()]/links/link[@rel='more']/@href"
            );
        }
    }

    /**
     * Turn XML into a message.
     * @param xml The XML
     * @return Message
     */
    private static Message msg(final XML xml) {
        // @checkstyle AnonInnerLengthCheck (50 lines)
        return new Message() {
            @Override
            public long number() {
                return Long.parseLong(
                    xml.xpath("number/text()").get(0)
                );
            }
            @Override
            public Date date() throws IOException {
                try {
                    return DateFormatUtils.ISO_DATETIME_FORMAT.parse(
                        xml.xpath("date/text()").get(0)
                    );
                } catch (final ParseException ex) {
                    throw new IOException(ex);
                }
            }
            @Override
            public String text() {
                return xml.xpath("text/text()").get(0);
            }
            @Override
            public String author() {
                return xml.xpath("author/text()").get(0);
            }
        };
    }

}
