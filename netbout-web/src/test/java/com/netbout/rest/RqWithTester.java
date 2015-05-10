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
package com.netbout.rest;

import com.jcabi.urn.URN;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import org.takes.Request;
import org.takes.facets.auth.Identity;
import org.takes.facets.auth.TkAuth;
import org.takes.facets.auth.codecs.CcPlain;
import org.takes.rq.RqFake;
import org.takes.rq.RqWithHeader;
import org.takes.rq.RqWrap;

/**
 * Request with tester on board.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.14
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@EqualsAndHashCode(callSuper = true)
public final class RqWithTester extends RqWrap {

    /**
     * Ctor.
     * @param urn URN of the tester
     * @throws IOException If fails
     */
    public RqWithTester(final URN urn) throws IOException {
        this(urn, new RqFake());
    }

    /**
     * Ctor.
     * @param urn URN of the tester
     * @param req Request
     * @throws IOException If fails
     */
    public RqWithTester(final URN urn, final Request req) throws IOException {
        super(RqWithTester.make(urn, req));
    }

    /**
     * Ctor.
     * @param urn URN of the tester
     * @param req Request
     * @return Request
     * @throws IOException If fails
     */
    private static Request make(final URN urn, final Request req)
        throws IOException {
        return new RqWithHeader(
            req,
            TkAuth.class.getSimpleName(),
            new String(
                new CcPlain().encode(
                    new Identity.Simple(urn.toString())
                )
            )
        );
    }
}
