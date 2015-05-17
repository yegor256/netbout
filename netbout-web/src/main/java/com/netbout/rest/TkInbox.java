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
package com.netbout.rest;

import com.google.common.collect.Iterables;
import com.netbout.spi.Base;
import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import com.netbout.spi.Inbox;
import java.io.IOException;
import java.util.Iterator;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.misc.Href;
import org.takes.rq.RqHref;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeDirectives;
import org.takes.rs.xe.XeLink;
import org.takes.rs.xe.XeSource;
import org.takes.rs.xe.XeTransform;
import org.xembly.Directives;

/**
 * Index.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.14
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
final class TkInbox implements Take {

    /**
     * Base.
     */
    private final transient Base base;

    /**
     * Ctor.
     * @param bse Base
     */
    TkInbox(final Base bse) {
        this.base = bse;
    }

    @Override
    public Response act(final Request req) throws IOException {
        return new RsPage(
            "/xsl/inbox.xsl",
            this.base,
            req,
            new XeAppend("bouts", this.bouts(req))
        );
    }

    /**
     * All bouts in the inbox.
     * @param req Request
     * @return Bouts
     * @throws IOException If fails
     */
    private Iterable<XeSource> bouts(final Request req) throws IOException {
        long since = Inbox.NEVER;
        final Iterator<String> param = new RqHref.Base(req).href()
            .param("since").iterator();
        if (param.hasNext()) {
            since = Long.parseLong(param.next());
        }
        return new XeTransform<>(
            Iterables.limit(
                new RqAlias(this.base, req).alias()
                    .inbox().jump(since).iterate(),
                Inbox.PAGE
            ),
            new XeTransform.Func<Bout>() {
                @Override
                public XeSource transform(final Bout bout) throws IOException {
                    return TkInbox.source(bout);
                }
            }
        );
    }

    /**
     * Convert bout to bundle.
     * @param bout Bout to convert
     * @return Bundle
     * @throws IOException If fails
     */
    private static XeSource source(final Bout bout) throws IOException {
        return new XeAppend(
            "bout",
            new XeDirectives(
                new Directives()
                    .add("number")
                    .set(Long.toString(bout.number())).up()
                    .add("updated")
                    .set(Long.toString(bout.updated().getTime())).up()
                    .add("unread")
                    .set(Long.toString(bout.messages().unread())).up()
                    .add("unseen")
                    .set(Integer.toString(bout.attachments().unseen())).up()
                    .add("title")
                    .set(bout.title()).up()
            ),
            new XeLink("open", new Href("/b").path(bout.number())),
            new XeLink(
                "more",
                new Href().with("since", bout.updated().getTime())
            ),
            new XeAppend(
                "friends",
                new XeTransform<>(
                    bout.friends().iterate(),
                    new XeTransform.Func<Friend>() {
                        @Override
                        public XeSource transform(final Friend friend)
                            throws IOException {
                            return TkInbox.source(bout, friend);
                        }
                    }
                )
            )
        );
    }

    /**
     * Convert friend to Xembly source.
     * @param bout The bout
     * @param friend The friend
     * @return Xembly source
     * @throws IOException If fails
     */
    private static XeSource source(final Bout bout, final Friend friend)
        throws IOException {
        return new XeAppend(
            "friend",
            new XeDirectives(
                new Directives().add("alias").set(friend.alias())
            ),
            new XeLink(
                "photo",
                new Href("/f").path(String.format("%s.png", friend.alias()))
            ),
            new XeLink(
                "kick",
                new Href("/b")
                    .path(bout.number())
                    .path("/kick")
                    .with("name", friend.alias())
            )
        );
    }

}
