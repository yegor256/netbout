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
package com.netbout.client;

import com.netbout.spi.Alias;
import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import com.netbout.spi.Friends;
import com.netbout.spi.Inbox;
import com.netbout.spi.User;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;

/**
 * Integration case for {@link RtFriends}.
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 0.14
 */
public final class RtFriendsITCase {

    /**
     * Netbout rule.
     * @checkstyle VisibilityModifierCheck (3 lines)
     */
    @Rule
    public final transient NbRule rule = new NbRule();

    /**
     * RtFriends can invite and kick off.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void invitesAndKicksOff() throws Exception {
        final User user = this.rule.get();
        final Alias alias = user.aliases().iterate().iterator().next();
        final Inbox inbox = alias.inbox();
        final Bout bout = inbox.bout(inbox.start());
        final String friend = "alex";
        bout.friends().invite(friend);
        MatcherAssert.assertThat(
            bout.friends().iterate(),
            Matchers.<Friend>iterableWithSize(2)
        );
        bout.friends().kick(friend);
        MatcherAssert.assertThat(
            bout.friends().iterate(),
            Matchers.<Friend>iterableWithSize(1)
        );
    }

    /**
     * RtFriends can make an exception when unknown alias invited.
     * @throws Exception If there is some problem inside
     */
    @Test (expected = Friends.UnknownAliasException.class)
    public void makesExceptionUnknownInvited() throws Exception {
        final User user = this.rule.get();
        final Alias alias = user.aliases().iterate().iterator().next();
        final Inbox inbox = alias.inbox();
        final Bout bout = inbox.bout(inbox.start());
        final String friend = "unknown";
        bout.friends().invite(friend);
    }
}
