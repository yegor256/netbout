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
import com.netbout.spi.Aliases;
import com.netbout.spi.Bout;
import com.netbout.spi.Inbox;
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
 * @since 2.14.17
 * @checkstyle ClassDataAbstractionCouplingCheck (100 lines)
 *
 */
public final class TkInboxTest {

    /**
     * TkInbox can kick an User.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void kicksAnUser() throws Exception {
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
                        RqMethod.GET,
                        String.format(
                            "/b/%d/kick?name=%s",
                            bout.number(),
                            alias
                        )
                    )
                )
            ).printHead(),
            Matchers.containsString("you+kicked")
        );
    }

    /**
     * TkInbox can search bouts.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void searchesBouts() throws Exception {
        final String urn = "urn:test:2";
        final MkBase base = new MkBase();
        final Aliases aliases = base.user(new URN(urn)).aliases();
        aliases.add("test2");
        final Inbox inbox = aliases.iterate().iterator().next().inbox();
        final Bout firstbout = inbox.bout(inbox.start());
        final String firsttitle = "bout1 title";
        firstbout.rename(firsttitle);
        final Bout secbout = inbox.bout(inbox.start());
        final String sectitle = "bout2 title";
        secbout.rename(sectitle);
        firstbout.messages().post("hello");
        secbout.messages().post("world");
        final String body = new RsPrint(
            new TkAuth(
                new TkInbox(base),
                new PsFixed(new Identity.Simple(urn))
            ).act(
                new RqFake(
                    RqMethod.GET,
                    String.format(
                        "/search?q=%s",
                        "r"
                    )
                )
            )
        ).printBody();
        MatcherAssert.assertThat(
            body,
            Matchers.containsString(sectitle)
        );
        MatcherAssert.assertThat(
            body,
            Matchers.not(Matchers.containsString(firsttitle))
        );
    }

    /**
     * TkInbox can handle invalid 'since' filter.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void handleInvalidSince() throws Exception {
        final String alias = "test3";
        final String urn = "urn:test:3";
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
                        RqMethod.GET,
                        "/?since"
                    )
                )
            ).printHead(),
            Matchers.containsString(
                "invalid+%27since%27+value%2C+timestamp+is+expected"
            )
        );
    }

    /**
     * TkInbox can handle valid 'since' filter.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void handleValidSince() throws Exception {
        final String alias = "test4";
        final String urn = "urn:test:4";
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
                        RqMethod.GET,
                        "/?since=123456789"
                    )
                )
            ).printHead(),
            Matchers.startsWith(
                "HTTP/1.1 200 OK"
            )
        );
    }

}
