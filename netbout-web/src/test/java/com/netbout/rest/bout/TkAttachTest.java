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

import com.jcabi.urn.URN;
import com.netbout.mock.MkBase;
import com.netbout.spi.Alias;
import com.netbout.spi.User;
import java.io.ByteArrayInputStream;
import org.junit.Test;
import org.takes.facets.auth.RqWithAuth;
import org.takes.facets.forward.RsFailure;
import org.takes.rq.RqFake;
import org.takes.rq.RqLive;
import org.takes.rq.RqMultipart;
import org.takes.rq.RqWithHeaders;

/**
 * Test case for {@link TkAttach}.
 *
 * @author Endrigo Antonini (teamed@endrigo.com.br)
 * @version $Id$
 * @since 2.15.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkAttachTest {

    /**
     * TkAttach can ignores wrong request.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = RsFailure.class)
    public void ignoresWrongRequest() throws Exception {
        final MkBase base = new MkBase();
        final String urn = "urn:test:1";
        final User user = base.user(new URN(urn));
        user.aliases().add("jeff");
        final Alias alias = user.aliases().iterate().iterator().next();
        final long bout = alias.inbox().start();
        alias.inbox().bout(bout).friends().invite(alias.name());
        new FkBout(".*", new TkAttach(base)).route(
            new RqWithAuth(
                urn,
                new RqMultipart.Fake(
                    new RqFake(
                        "POST",
                        String.format("/b/%d/attach", bout)
                    ),
                    new RqWithHeaders(
                        new RqLive(
                            new ByteArrayInputStream("content".getBytes())
                        ),
                        String.format("POST /b/%d/attach HTTP/1.1", bout),
                        //@checkstyle LineLengthCheck (1 line)
                        "Content-Disposition: form-data; name=\"file\"; filenam=\"aBaaPDF.pdf\"",
                        "Content-Type: application/pdf"
                    )
                )
            )
        );
    }
}
