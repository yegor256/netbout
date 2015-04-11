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
package com.netbout.rest.bout;

import com.google.common.collect.Iterables;
import com.netbout.rest.Markdown;
import com.netbout.rest.RsPage;
import com.netbout.spi.Attachment;
import com.netbout.spi.Base;
import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import com.netbout.spi.Inbox;
import com.netbout.spi.Message;
import com.netbout.spi.Messages;
import java.io.IOException;
import java.util.Iterator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.ocpsoft.prettytime.PrettyTime;
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
import org.takes.rs.xe.XeWhen;
import org.xembly.Directives;

/**
 * Index.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 2.14
 */
final class TkIndex implements Take {

    /**
     * Base.
     */
    private final transient Base base;

    /**
     * Bout.
     */
    private final transient Bout bout;

    /**
     * Ctor.
     * @param bot Bout
     */
    TkIndex(final Base bse, final Bout bot) {
        this.base = bse;
        this.bout = bot;
    }

    @Override
    public Response act(final Request req) throws IOException {
        long start = Inbox.NEVER;
        final Iterator<String> param = new RqHref.Base(req).href()
            .param("start").iterator();
        if (param.hasNext()) {
            start = Long.parseLong(param.next());
        }
        final Href home = new Href("/b").path(this.bout.number());
        return new RsPage(
            "/xsl/bout.xsl",
            this.base,
            req,
            new XeAppend(
                "bouts",
                new XeDirectives(
                    new Directives()
                        .add("number")
                        .set(Long.toString(this.bout.number()))
                        .up()
                        .add("title").set(this.bout.title()).up()
                        .add("unread")
                        .set(Long.toString(this.bout.messages().unread()))
                ),
                new XeAppend(
                    "friends",
                    new XeTransform<Friend>(
                        this.bout.friends().iterate(),
                        new XeTransform.Func<Friend>() {
                            @Override
                            public XeSource transform(final Friend friend)
                                throws IOException {
                                return TkIndex.this.source(friend);
                            }
                        }
                    )
                ),
                new XeAppend(
                    "attachments",
                    new XeTransform<Attachment>(
                        this.bout.attachments().iterate(),
                        new XeTransform.Func<Attachment>() {
                            @Override
                            public XeSource transform(final Attachment atmt)
                                throws IOException {
                                return TkIndex.this.source(req, atmt);
                            }
                        }
                    )
                ),
                new XeAppend(
                    "messages",
                    new XeTransform<Message>(
                        Iterables.limit(
                            this.bout.messages().jump(start).iterate(),
                            Messages.PAGE
                        ),
                        new XeTransform.Func<Message>() {
                            @Override
                            public XeSource transform(final Message msg)
                                throws IOException {
                                return TkIndex.this.source(msg);
                            }
                        }
                    )
                )
            ),
            new XeLink("post", home.path("post")),
            new XeLink("rename", home.path("rename")),
            new XeLink("invite", home.path("invite")),
            new XeLink("upload", home.path("upload")),
            new XeLink("create", home.path("create")),
            new XeLink("attach", home.path("attach"))
        );
    }

    /**
     * Convert friend to Xembly.
     * @param friend Friend to convert
     * @return Xembly
     * @throws IOException If fails
     */
    private XeSource source(final Friend friend) throws IOException {
        return new XeAppend(
            "friend",
            new XeDirectives(
                new Directives().add("alias").set(friend.alias())
            ),
            new XeLink(
                "photo",
                new Href().path("f").path(
                    String.format("%s.png", friend.alias())
                )
            ),
            new XeLink(
                "kick",
                new Href().path("b")
                    .path(this.bout.number())
                    .path("kick")
                    .with("name", friend.alias())
            )
        );
    }

    /**
     * Convert attachment to Xembly source.
     * @param req Request
     * @param atmt Attachment
     * @return Xembly source
     * @throws IOException If fails
     */
    private XeSource source(final Request req, final Attachment atmt)
        throws IOException {
        String open = "";
        final Iterator<String> param = new RqHref.Base(req).href()
            .param("open").iterator();
        if (param.hasNext()) {
            open = param.next();
        }
        return new XeAppend(
            "attachment",
            new XeDirectives(
                new Directives()
                    .add("name").set(atmt.name()).up()
                    .add("ctype").set(atmt.ctype()).up()
                    .add("etag").set(atmt.etag()).up()
                    .add("unseen").set(Boolean.toString(atmt.unseen()))
            ),
            new XeLink(
                "delete",
                new Href().path("b")
                    .path(this.bout.number())
                    .path("delete")
                    .with("name", atmt.name())
            ),
            new XeLink(
                "download",
                new Href().path("b")
                    .path(this.bout.number())
                    .path("download")
                    .with("name", atmt.name())
            ),
            new XeWhen(
                atmt.ctype().equals(Attachment.MARKDOWN),
                new XeLink(
                    "open",
                    new Href().path("b")
                        .path(this.bout.number())
                        .path("open")
                        .with("name", atmt.name())
                )
            ),
            new XeWhen(
                atmt.name().equals(open)
                    && atmt.ctype().equals(Attachment.MARKDOWN),
                new XeDirectives(
                    new Directives().add("html").set(
                        new Markdown(
                            IOUtils.toString(
                                atmt.read(),
                                CharEncoding.UTF_8
                            )
                        ).html()
                    )
                )
            )
        );
    }

    /**
     * Convert message to Xembly source.
     * @param message Message
     * @return Xembly source
     * @throws IOException In case of failure
     */
    private XeSource source(final Message message) throws IOException {
        return new XeAppend(
            "message",
            new XeDirectives(
                new Directives()
                    .add("number").set(Long.toString(message.number()))
                    .up()
                    .add("author").set(message.author()).up()
                    .add("text").set(message.text()).up()
                    .add("html").set(new Markdown(message.text()).html()).up()
                    .add("timeago")
                    .set(new PrettyTime().format(message.date())).up()
                    .add("date")
                    .set(
                        DateFormatUtils.ISO_DATETIME_FORMAT.format(
                            message.date()
                        )
                    )
            ),
            new XeLink(
                "photo",
                new Href().path("f").path(
                    String.format("%s.png", message.author())
                )
            ),
            new XeLink(
                "more",
                new Href().path("b")
                    .path(this.bout.number())
                    .with("start", message.number())
            )
        );
    }

}
