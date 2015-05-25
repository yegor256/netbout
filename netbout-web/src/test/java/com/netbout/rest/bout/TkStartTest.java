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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.jcabi.urn.URN;
import com.netbout.mock.MkBase;
import com.netbout.rest.RqWithTester;
import com.netbout.rest.TkStart;
import com.netbout.spi.Alias;
import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import com.netbout.spi.Message;
import com.netbout.spi.User;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.takes.facets.forward.TkForward;
import org.takes.rq.RqFake;
import org.takes.rs.RsPrint;

/**
 * Test case for {@link TkStart}.
 * @author Ivan Inozemtsev (ivan.inozemtsev@gmail.com)
 * @version $Id$
 * @todo #610:30min Test case for negative scenario of TkStart
 *  should be added, e.g. when a friend is not found, the error
 *  should be reported correctly and bout creation should be
 *  discarded.
 * @since 2.15
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkStartTest {

    /**
     * TkStart can post message and invite friends to
     * newly created bouts.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void postsMessageAndInvitesFriends() throws Exception {
        final MkBase base = new MkBase();
        final URN urn = new URN("urn:test:1");
        final User user = base.user(urn);
        final String name = "Jeff";
        user.aliases().add(name);
        final Alias[] friends = new Alias[] {
            base.randomAlias(),
            base.randomAlias(),
        };
        final String post = "message";
        final String head = new RsPrint(
            new TkForward(new TkStart(base)).act(
                new RqWithTester(
                    urn,
                    new RqFake(
                        "GET",
                        String.format(
                            "/start?post=%s&invite=%s&invite=%s",
                            post,
                            friends[0].name(),
                            friends[1].name()
                        )
                    )
                )
            )
        ).printHead();
        final Matcher matcher = Pattern.compile(
            "^Location:.*/b/(\\d+)$",
            Pattern.MULTILINE
        ).matcher(head);
        MatcherAssert.assertThat("Location header is correct", matcher.find());
        final Bout bout = user.aliases().iterate().iterator().next().inbox()
            .bout(Long.parseLong(matcher.group(1)));
        final Message message = bout.messages().iterate().iterator().next();
        MatcherAssert.assertThat(message.author(), Matchers.equalTo(name));
        MatcherAssert.assertThat(message.text(), Matchers.equalTo(post));
        MatcherAssert.assertThat(
            Iterables.transform(
                bout.friends().iterate(),
                new Function<Friend, String>() {
                    @Override
                    public String apply(final Friend friend) {
                        try {
                            return friend.alias();
                        } catch (final IOException ex) {
                            throw new IllegalStateException(ex);
                        }
                    }
                }
            ),
            Matchers.containsInAnyOrder(friends[0].name(), friends[1].name())
        );
    }
}
