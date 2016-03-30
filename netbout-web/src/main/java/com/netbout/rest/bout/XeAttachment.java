/**
 * Copyright (c) 2009-2016, netbout.com
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

import com.netbout.rest.Markdown;
import com.netbout.spi.Attachment;
import com.netbout.spi.Bout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import org.apache.commons.io.IOUtils;
import org.takes.Request;
import org.takes.misc.Href;
import org.takes.rq.RqHref;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeDirectives;
import org.takes.rs.xe.XeLink;
import org.takes.rs.xe.XeSource;
import org.takes.rs.xe.XeWhen;
import org.takes.rs.xe.XeWrap;
import org.xembly.Directive;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Attachment in Xembly.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.14
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class XeAttachment extends XeWrap {

    /**
     * Ctor.
     * @param req Request
     * @param bout Bout
     * @param atmt Attachment
     * @throws IOException If fails
     */
    XeAttachment(final Request req, final Bout bout,
        final Attachment atmt) throws IOException {
        super(XeAttachment.make(req, bout, atmt));
    }

    /**
     * Convert attachment to Xembly source.
     * @param req Request
     * @param bout Bout
     * @param atmt Attachment
     * @return Xembly source
     * @throws IOException If fails
     */
    private static XeSource make(final Request req, final Bout bout,
        final Attachment atmt) throws IOException {
        String open = "";
        final Iterator<String> param = new RqHref.Base(req).href()
            .param("open").iterator();
        if (param.hasNext()) {
            open = param.next();
        }
        final String name = "name";
        return new XeAppend(
            "attachment",
            new XeDirectives(
                new Directives()
                    .add("name").set(atmt.name()).up()
                    .add("ctype").set(atmt.ctype()).up()
                    .add("etag").set(atmt.etag()).up()
                    .add("unseen").set(Boolean.toString(atmt.unseen())).up()
                    .add("author").set(atmt.author()).up()
                    .add("date").set(atmt.date().getTime())
            ),
            new XeLink(
                "delete",
                new Href().path("b")
                    .path(bout.number())
                    .path("delete")
                    .with(name, atmt.name())
            ),
            new XeLink(
                "download",
                new Href().path("b")
                    .path(bout.number())
                    .path("download")
                    .with(name, atmt.name())
            ),
            new XeWhen(
                atmt.ctype().equals(Attachment.MARKDOWN),
                new XeLink(
                    "open",
                    new Href().path("b")
                        .path(bout.number())
                        .with("open", atmt.name())
                )
            ),
            new XeWhen(
                atmt.name().equals(open)
                    && atmt.ctype().equals(Attachment.MARKDOWN),
                new XeSource() {
                    @Override
                    public Iterable<Directive> toXembly() throws IOException {
                        return new Directives().add("html").set(
                            Xembler.escape(
                                new Markdown.Default().html(
                                    IOUtils.toString(
                                        atmt.read(),
                                        StandardCharsets.UTF_8
                                    )
                                )
                            )
                        );
                    }
                }
            )
        );
    }

}
