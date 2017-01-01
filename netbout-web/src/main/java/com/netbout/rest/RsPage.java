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
package com.netbout.rest;

import com.netbout.spi.Base;
import java.io.IOException;
import java.util.Iterator;
import lombok.EqualsAndHashCode;
import org.takes.Request;
import org.takes.Response;
import org.takes.facets.fork.FkTypes;
import org.takes.facets.fork.Fork;
import org.takes.facets.fork.RsFork;
import org.takes.misc.Opt;
import org.takes.rq.RqHeaders;
import org.takes.rs.RsPrettyXML;
import org.takes.rs.RsWithType;
import org.takes.rs.RsWrap;
import org.takes.rs.RsXSLT;
import org.takes.rs.xe.RsXembly;
import org.takes.rs.xe.XeSource;
import org.takes.rs.xe.XeStylesheet;

/**
 * Index resource, front page of the website.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.14
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class RsPage extends RsWrap {

    /**
     * Ctor.
     * @param xsl XSL
     * @param base Base
     * @param req Request
     * @param src Source
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public RsPage(final String xsl, final Base base,
        final Request req, final XeSource... src) throws IOException {
        super(RsPage.make(xsl, base, req, src));
    }

    /**
     * Make it.
     * @param xsl XSL
     * @param base Base
     * @param req Request
     * @param src Source
     * @return Response
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    private static Response make(final String xsl, final Base base,
        final Request req, final XeSource... src) throws IOException {
        final Response xbl = new RsXembly(
            new XeStylesheet(xsl),
            new XePage(base, req, src)
        );
        final Response raw = new RsWithType(xbl, "text/xml");
        return new RsPrettyXML(
            new RsFork(
                req,
                new Fork() {
                    @Override
                    public Opt<Response> route(final Request rst)
                        throws IOException {
                        final RqHeaders hdr = new RqHeaders.Base(rst);
                        final Iterator<String> agent =
                            hdr.header("User-Agent").iterator();
                        final Opt<Response> opt;
                        if (agent.hasNext()
                            && agent.next().contains("Firefox")) {
                            opt = new Opt.Single<Response>(
                                new RsXSLT(new RsWithType(raw, "text/html"))
                            );
                        } else {
                            opt = new Opt.Empty<>();
                        }
                        return opt;
                    }
                },
                new FkTypes("application/xml,text/xml", raw),
                new FkTypes(
                    "*/*",
                    new RsXSLT(new RsWithType(raw, "text/html"))
                )
            )
        );
    }

}
