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

import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.urn.URN;
import com.netbout.mock.MkBase;
import com.netbout.spi.Alias;
import com.netbout.spi.Bout;
import com.netbout.spi.User;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.takes.facets.auth.RqWithAuth;
import org.takes.rq.RqFake;
import org.takes.rs.RsPrint;

/**
 * Test case for {@link TkIndex}.
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.15
 */
public final class TkIndexTest {

    /**
     * TkIndex can render bout page.
     * @throws Exception If there is some problem inside
     * @checkstyle MultipleStringLiteralsCheck (76 lines)
     */
    @Test
    public void rendersBoutPage() throws Exception {
        final MkBase base = new MkBase();
        final String urn = "urn:test:1";
        final User user = base.user(new URN(urn));
        user.aliases().add("jeff");
        final Alias alias = user.aliases().iterate().iterator().next();
        final Bout bout = alias.inbox().bout(alias.inbox().start());
        bout.attachments().create("a1");
        bout.messages().post("hello, world!");
        bout.friends().invite(alias.name());
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new RsPrint(
                    new FkBout(".*", new TkIndex(base)).route(
                        new RqWithAuth(
                            urn,
                            new RqFake(
                                "GET",
                                String.format("/b/%d", bout.number())
                            )
                        )
                    ).get()
                ).printBody()
            ),
            XhtmlMatchers.hasXPaths(
                "/page/bout[number=1]",
                "/page/bout[title='untitled']",
                "/page/bout[unread=0]",
                "/page/bout/friends/friend[alias='jeff']",
                "/page/bout/friends/friend/links/link[@rel='photo']",
                "/page/bout/friends/friend/links/link[@rel='kick']",
                "/page/bout/attachments/attachment/links/link[@rel='delete']",
                "/page/bout/messages/message[text='hello, world!']"
            )
        );
    }
    /**
     * TkIndex can search bout messages.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void searchesMessages() throws Exception {
        final MkBase base = new MkBase();
        final String urn = "urn:test:99";
        final User user = base.user(new URN(urn));
        user.aliases().add("test-user");
        final Alias alias = user.aliases().iterate().iterator().next();
        final Bout bout = alias.inbox().bout(alias.inbox().start());
        bout.messages().post("test1");
        bout.messages().post("test2");
        bout.messages().post("fest");
        bout.friends().invite(alias.name());
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new RsPrint(
                    new FkBout(".*", new TkIndex(base)).route(
                        new RqWithAuth(
                            urn,
                            new RqFake(
                                "GET",
                                String.format(
                                    "/b/%d/search?q=test",
                                    bout.number()
                                )
                            )
                        )
                    ).get()
                ).printBody()
            ),
            XhtmlMatchers.hasXPaths(
                "/page/bout[number=1]",
                "/page/bout[title='untitled']",
                "/page/bout[unread=0]",
                "/page/bout/friends/friend[alias='test-user']",
                "/page/bout/friends/friend/links/link[@rel='photo']",
                "/page/bout/friends/friend/links/link[@rel='kick']",
                "/page/bout/messages/message[text='test2']",
                "/page/bout/messages/message[text='test1']"
            )
        );
    }

}
