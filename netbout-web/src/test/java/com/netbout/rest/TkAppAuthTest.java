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

import com.jcabi.manifests.Manifests;
import javax.xml.bind.DatatypeConverter;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.takes.facets.auth.PsFake;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqFake;
import org.takes.rq.RqMethod;
import org.takes.rq.RqWithHeaders;
import org.takes.rs.RsPrint;
import org.takes.tk.TkText;

/**
 * Test case for {@link TkAppAuth}.
 * @author Dmitry Zaytsev (dmitry.zaytsev@gmail.com)
 * @version $Id$
 * @since 2.17
 */
public final class TkAppAuthTest {
    /**
     * TkAppAuth can skip BasicAuth step.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void skipsBasicAuth() throws Exception {
        final String text = "zz";
        MatcherAssert.assertThat(
            new RsPrint(
                new TkAppAuth(new TkText(text), new PsFake(true), false).act(
                    new RqFake(RqMethod.GET, "/")
                )
            ).printBody(),
            Matchers.equalTo(text)
        );
    }

    /**
     * TkAppAuth can accept BasicAuth.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void acceptsBasicAuth() throws Exception {
        final String text = "yy";
        MatcherAssert.assertThat(
            new RsPrint(
                new TkAppAuth(new TkText(text), new PsFake(false), true).act(
                    new RqWithHeaders(
                        new RqFake(RqMethod.GET, "?code=1"),
                        TkAppAuthTest.generateAuthenticateHead(
                            Manifests.read("Netbout-Basic-User"),
                            Manifests.read("Netbout-Basic-Pwd")
                        )
                    )
                )
            ).printBody(),
            Matchers.equalTo(text)
        );
    }

    /**
     * TkAppAuth can reject invalid BasicAuth password.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = RsForward.class)
    public void rejectsBasicAuth() throws Exception {
        new TkAppAuth(new TkText("xx"), new PsFake(false), true).act(
            new RqWithHeaders(
                new RqFake(RqMethod.GET, "?code=2"),
                TkAppAuthTest.generateAuthenticateHead(
                    "invalid user",
                    "invalid password"
                )
            )
        );
    }

    /**
     * Generate the string used on the request that store information about
     * authentication.
     * @param user Username
     * @param pass Password
     * @return Header string.
     */
    private static String generateAuthenticateHead(
        final String user,
        final String pass
    ) {
        final String auth = String.format("%s:%s", user, pass);
        final String encoded = DatatypeConverter.printBase64Binary(
            auth.getBytes()
        );
        return String.format("Authorization: Basic %s", encoded);
    }
}
