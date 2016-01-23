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
package com.netbout.rest;

import com.jcabi.urn.URN;
import com.netbout.mock.MkBase;
import com.netbout.spi.Bout;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.takes.facets.auth.Identity;
import org.takes.facets.auth.PsFixed;
import org.takes.facets.auth.TkAuth;
import org.takes.rq.RqFake;
import org.takes.rq.RqMethod;
import org.takes.rs.RsPrint;

/**
 * Test case for {@link TkPreview}.
 * @author Endrigo Antonini (teamed@endrigo.com.br)
 * @version $Id$
 * @since 2.15.2
 * @checkstyle ClassDataAbstractionCouplingCheck (100 lines)
 */
public final class TkPreviewTest {

    /**
     * TkPreview can preview message before post.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void previewMessageBeforePost() throws Exception {
        final String alias = "test";
        final String urn = "urn:test:1";
        final MkBase base = new MkBase();
        final Bout bout = base.randomBout();
        base.user(new URN(urn)).aliases().add(alias);
        bout.friends().invite(alias);
        MatcherAssert.assertThat(
            new RsPrint(
                new TkAuth(
                    new TkApp(base),
                    new PsFixed(new Identity.Simple(urn))
                ).act(
                    new RqFake(
                        RqMethod.POST,
                        String.format(
                            "/b/%d/preview",
                            bout.number()
                        ),
                        "text=Here is a paragraph.\n\nNew paragraph."
                    )
                )
            ).printBody(),
            Matchers.allOf(
                Matchers.containsString("<p>Here is a paragraph.<br  /></p>"),
                Matchers.containsString("<p>New paragraph.</p>")
            )
        );
    }
}
