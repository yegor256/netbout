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

import com.netbout.spi.Alias;
import com.netbout.spi.Base;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import org.takes.Request;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeChain;
import org.takes.rs.xe.XeDirectives;
import org.takes.rs.xe.XeLink;
import org.takes.rs.xe.XeSource;
import org.takes.rs.xe.XeWhen;
import org.takes.rs.xe.XeWrap;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Xembly for alias.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.14
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@EqualsAndHashCode(callSuper = true)
final class XeAlias extends XeWrap {

    /**
     * Ctor.
     * @param base The base
     * @param req Request
     * @throws IOException If fails
     */
    XeAlias(final Base base, final Request req) throws IOException {
        super(XeAlias.source(base, req));
    }

    /**
     * Make xembly for account.
     * @param base The base
     * @param req Request
     * @return Xembly source
     * @throws IOException If fails
     */
    private static XeSource source(final Base base,
        final Request req) throws IOException {
        final RqAlias rqa = new RqAlias(base, req);
        return new XeWhen(
            rqa.has(),
            new XeSource() {
                @Override
                public Iterable<Directive> toXembly() throws IOException {
                    return new XeChain(
                        XeAlias.source(rqa.alias()),
                        new XeLink("start", "/start"),
                        new XeLink("account", "/acc/index")
                    ).toXembly();
                }
            }
        );
    }

    /**
     * Make xembly for alias.
     * @param alias Alias
     * @return Xembly source
     * @throws IOException If fails
     */
    private static XeSource source(final Alias alias) throws IOException {
        final String email;
        if (alias.email().contains("!")) {
            email = alias.email().substring(0, alias.email().indexOf('!'));
        } else {
            email = alias.email();
        }
        return new XeAppend(
            "alias",
            new XeDirectives(
                new Directives()
                    .add("name").set(alias.name()).up()
                    .add("locale").set(alias.locale().toString()).up()
                    .add("photo").set(alias.photo().toString()).up()
                    .add("email").set(email)
            )
        );
    }

}
