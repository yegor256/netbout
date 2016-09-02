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
import com.netbout.spi.Bout;
import com.netbout.spi.Message;
import java.io.IOException;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.ocpsoft.prettytime.PrettyTime;
import org.takes.misc.Href;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeDirectives;
import org.takes.rs.xe.XeLink;
import org.takes.rs.xe.XeSource;
import org.takes.rs.xe.XeWrap;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Message in Xembly.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.14
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class XeMessage extends XeWrap {

    /**
     * Ctor.
     * @param bout Bout
     * @param msg Message
     * @throws IOException In case of failure
     */
    XeMessage(final Bout bout, final Message msg)
        throws IOException {
        super(XeMessage.make(bout, msg));
    }

    /**
     * Convert message to Xembly source.
     * @param bout Bout
     * @param msg Message
     * @return Xembly source
     * @throws IOException In case of failure
     */
    private static XeSource make(final Bout bout, final Message msg)
        throws IOException {
        return new XeAppend(
            "message",
            new XeDirectives(
                new Directives()
                    .add("number")
                    .set(Long.toString(msg.number()))
                    .up()
                    .add("author").set(Xembler.escape(msg.author())).up()
                    .add("text").set(Xembler.escape(msg.text())).up()
                    .add("html")
                    .set(
                        Xembler.escape(
                            new Markdown.Default().html(msg.text())
                        )
                    ).up()
                    .add("timeago")
                    .set(new PrettyTime().format(msg.date())).up()
                    .add("date")
                    .set(
                        DateFormatUtils.ISO_DATETIME_FORMAT.format(
                            msg.date()
                        )
                    )
            ),
            new XeLink(
                "photo",
                new Href().path("f").path(
                    String.format("%s.png", msg.author())
                )
            ),
            new XeLink(
                "more",
                new Href().path("b")
                    .path(bout.number())
                    .with("start", msg.number())
            )
        );
    }

}
