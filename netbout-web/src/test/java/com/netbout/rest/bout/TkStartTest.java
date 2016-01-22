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
package com.netbout.rest.bout;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.jcabi.urn.URN;
import com.netbout.mock.MkBase;
import com.netbout.rest.TkStart;
import com.netbout.spi.Alias;
import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import com.netbout.spi.Message;
import com.netbout.spi.User;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.CharEncoding;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.takes.facets.auth.RqWithAuth;
import org.takes.facets.forward.TkForward;
import org.takes.rq.RqFake;
import org.takes.rq.RqMethod;
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
     * Location pattern.
     */
    private static final Pattern LOCATION = Pattern.compile(
        "^Location:.*/b/(\\d+)$",
        Pattern.MULTILINE
    );
    /**
     * TkStart can post message and invite friends to
     * newly created bouts.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void postsMessageAndInvitesFriends() throws Exception {
        final MkBase base = new MkBase();
        final String urn = "urn:test:1";
        final User user = base.user(new URN(urn));
        final String name = "Jeff";
        user.aliases().add(name);
        final Alias[] friends = new Alias[] {
            base.randomAlias(),
            base.randomAlias(),
        };
        final String post = "message";
        final String head = new RsPrint(
            new TkForward(new TkStart(base)).act(
                new RqWithAuth(
                    urn,
                    new RqFake(
                        RqMethod.GET,
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
        final Matcher matcher = TkStartTest.LOCATION.matcher(head);
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
            Matchers.containsInAnyOrder(
                friends[0].name(),
                friends[1].name(),
                name
            )
        );
    }
    /**
     * TkStart can rename newly created bouts.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void renamesBout() throws Exception {
        final MkBase base = new MkBase();
        final String urn = "urn:test:2";
        final User user = base.user(new URN(urn));
        user.aliases().add("John");
        final String name = "let's talk";
        final String head = new RsPrint(
            new TkForward(new TkStart(base)).act(
                new RqWithAuth(
                    urn,
                    new RqFake(
                        RqMethod.GET,
                        String.format(
                            "/start?post=message&invite=%s&rename=%s",
                            base.randomAlias().name(),
                            URLEncoder.encode(name, CharEncoding.UTF_8)
                        )
                    )
                )
            )
        ).printHead();
        final Matcher matcher = TkStartTest.LOCATION.matcher(head);
        MatcherAssert.assertThat("Location header is valid", matcher.find());
        MatcherAssert.assertThat(
            user.aliases().iterate().iterator().next().inbox()
            .bout(Long.parseLong(matcher.group(1))).title(),
            Matchers.equalTo(name)
        );
    }

    /**
     * TkStart can prevent more then one rename.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = IOException.class)
    public void preventManyRenameParameters() throws Exception {
        final MkBase base = new MkBase();
        final String urn = "urn:test:3";
        final User user = base.user(new URN(urn));
        user.aliases().add("Alex");
        new TkStart(base).act(
            new RqWithAuth(
                urn,
                new RqFake(
                    RqMethod.GET,
                    String.format(
                        "/start?post=message&invite=%s&rename=%s&rename=%s",
                        base.randomAlias().name(),
                        "first",
                        "second"
                    )
                )
            )
        );
    }

    /**
     * TkStart can handle multiple post parameters.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void handlesMultiplePosts() throws Exception {
        final MkBase base = new MkBase();
        final String urn = "urn:test:4";
        final User user = base.user(new URN(urn));
        user.aliases().add("Steve");
        final String[] msgs = new String[] {
            "message1",
            "message2",
        };
        final String head = new RsPrint(
            new TkForward(new TkStart(base)).act(
                new RqWithAuth(
                    urn,
                    new RqFake(
                        RqMethod.GET,
                        String.format(
                            "/start?post=%s&post=%s",
                            msgs[0],
                            msgs[1]
                        )
                    )
                )
            )
        ).printHead();
        final Matcher matcher = TkStartTest.LOCATION.matcher(head);
        MatcherAssert.assertThat("Loct header is valid", matcher.find());
        final Bout bout = user.aliases().iterate().iterator().next().inbox()
            .bout(Long.parseLong(matcher.group(1)));
        int posted = 0;
        final Iterator<Message> iterator = bout.messages().iterate().iterator();
        while (iterator.hasNext()) {
            final Message message = iterator.next();
            if (posted < msgs.length) {
                MatcherAssert.assertThat(
                    message.text(),
                    Matchers.equalTo(msgs[posted])
                );
            }
            ++posted;
        }
        MatcherAssert.assertThat(posted, Matchers.equalTo(msgs.length));
    }

    /**
     * TkStart can handle token parameter.
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void handlesToken() throws Exception {
        final MkBase base = new MkBase();
        final String urn = "urn:test:5";
        final User user = base.user(new URN(urn));
        user.aliases().add("Jane");
        final String[] msgs = {
            "Hello World",
            "I shouldn't be created as new",
            "I should be created as new",
            "I am new too",
            "Another new",
        };
        final String[] tokens = {
            // @checkstyle MultipleStringLiteralsCheck (2 lines)
            "13579",
            "13579",
            "24680",
            "",
            "",
        };
        final long[] loc = new long[msgs.length];
        for (int count = 0; count < msgs.length; count += 1) {
            final String head = new RsPrint(
                new TkForward(new TkStart(base)).act(
                    new RqWithAuth(
                        urn,
                        new RqFake(
                            RqMethod.GET,
                            String.format(
                                "/start?post=%s&token=%s",
                                URLEncoder.encode(
                                    msgs[count],
                                    CharEncoding.UTF_8
                                ),
                                URLEncoder.encode(
                                    tokens[count],
                                    CharEncoding.UTF_8
                                )
                            )
                        )
                    )
                )
            ).printHead();
            final Matcher matcher = TkStartTest.LOCATION.matcher(head);
            MatcherAssert.assertThat(
                "Found matching location header",
                matcher.find()
            );
            user.aliases().iterate().iterator().next().inbox()
                .bout(Long.parseLong(matcher.group(1)));
            loc[count] = Long.parseLong(matcher.group(1));
        }
        // @checkstyle MagicNumber (5 lines)
        MatcherAssert.assertThat(loc[0], Matchers.equalTo(loc[1]));
        MatcherAssert.assertThat(loc[0], Matchers.not(loc[2]));
        MatcherAssert.assertThat(loc[0], Matchers.not(loc[3]));
        MatcherAssert.assertThat(loc[2], Matchers.not(loc[3]));
        MatcherAssert.assertThat(loc[3], Matchers.not(loc[4]));
    }
}
