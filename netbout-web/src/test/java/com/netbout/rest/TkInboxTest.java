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
 * Test case for {@link TkInbox}.
 * @author Endrigo Antonini (teamed@endrigo.com.br)
 * @version $Id$
 * @todo #704:30min Test case for search bouts of TkInbox
 *  should be added, e.g. response should have filtered
 *  bouts by search term
 * @since 2.14.17
 * @checkstyle ClassDataAbstractionCouplingCheck (100 lines)
 *
 */
public final class TkInboxTest {

    /**
     * Alias used in the tests.
     */
    private static final String ALIAS = "test";

    /**
     * URN of the identity used in the tests.
     */
    private static final String URN = "urn:test:1";

    /**
     * TkInbox can kick an User.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void kicksAnUser() throws Exception {
        final MkBase base = new MkBase();
        final Bout bout = base.randomBout();
        base.user(new URN(URN)).aliases().add(ALIAS);
        bout.friends().invite(ALIAS);
        MatcherAssert.assertThat(
            new RsPrint(
                new TkAuth(
                    new TkApp(base),
                    new PsFixed(new Identity.Simple(URN))
                ).act(
                    new RqFake(
                        RqMethod.GET,
                        String.format(
                            "/b/%d/kick?name=%s",
                            bout.number(),
                            ALIAS
                        )
                    )
                )
            ).printHead(),
            Matchers.containsString("you+kicked")
        );
    }

    /**
     * TkInbox can search messages.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void searchesMessages() throws Exception {
        final MkBase base = new MkBase();
        final Bout bout = base.randomBout();
        base.user(new URN(URN)).aliases().add(ALIAS);
        bout.friends().invite(ALIAS);
        final String hello = "hello";
        final String world = "world";
        bout.messages().post(hello);
        bout.messages().post(world);
        final String body = new RsPrint(
            new TkAuth(
                    new TkApp(base),
                    new PsFixed(new Identity.Simple(URN))
            ).act(
                new RqFake(
                    RqMethod.GET,
                    String.format(
                        "/b/%d/search?q=%s",
                        bout.number(),
                        "r"
                    )
                )
            )
        ).printBody();
        MatcherAssert.assertThat(
            body,
            Matchers.containsString(world)
        );
        MatcherAssert.assertThat(
            body,
            Matchers.not(Matchers.containsString(hello))
        );
    }
}
