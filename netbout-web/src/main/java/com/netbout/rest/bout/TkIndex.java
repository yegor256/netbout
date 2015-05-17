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
package com.netbout.rest.bout;

import com.google.common.collect.Iterables;
import com.netbout.rest.RsPage;
import com.netbout.spi.Attachment;
import com.netbout.spi.Base;
import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import com.netbout.spi.Inbox;
import com.netbout.spi.Message;
import com.netbout.spi.Messages;
import java.io.IOException;
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
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class TkIndex implements Take {

    /**
     * Base.
     */
    private final transient Base base;

    /**
     * Ctor.
     * @param bse Base
     */
    TkIndex(final Base bse) {
        this.base = bse;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final long start = Long.parseLong(
            new RqHref.Smart(new RqHref.Base(req)).single(
                "start",
                Long.toString(Inbox.NEVER)
            )
        );
        final Bout bout = new RqBout(this.base, req).bout();
        final Href home = new Href("/b").path(bout.number());
        return new RsPage(
            "/xsl/bout.xsl",
            this.base,
            req,
            new XeAppend(
                "bout",
                new XeDirectives(
                    new Directives()
                        .add("number")
                        .set(Long.toString(bout.number()))
                        .up()
                        .add("title").set(bout.title()).up()
                        .add("unread")
                        .set(Long.toString(bout.messages().unread()))
                ),
                new XeAppend(
                    "friends",
                    new XeTransform<>(
                        bout.friends().iterate(),
                        new XeTransform.Func<Friend>() {
                            @Override
                            public XeSource transform(final Friend friend)
                                throws IOException {
                                return new XeFriend(bout, friend);
                            }
                        }
                    )
                ),
                new XeAppend(
                    "attachments",
                    new XeTransform<>(
                        bout.attachments().iterate(),
                        new XeTransform.Func<Attachment>() {
                            @Override
                            public XeSource transform(final Attachment atmt)
                                throws IOException {
                                return new XeAttachment(req, bout, atmt);
                            }
                        }
                    )
                ),
                new XeAppend(
                    "messages",
                    new XeTransform<>(
                        Iterables.limit(
                            bout.messages().jump(start).iterate(),
                            Messages.PAGE
                        ),
                        new XeTransform.Func<Message>() {
                            @Override
                            public XeSource transform(final Message msg)
                                throws IOException {
                                return new XeMessage(bout, msg);
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

}
