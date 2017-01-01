/**
 * Copyright (c) 2009-2017, netbout.com
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
package com.netbout.dynamo;

import com.jcabi.aspects.Tv;
import com.jcabi.urn.URN;
import com.netbout.spi.Aliases;
import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import com.netbout.spi.Friends;
import com.netbout.spi.Inbox;
import com.netbout.spi.Pageable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Integration case for {@link DyInbox}.
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 */
public final class DyInboxITCase {
    /**
     * DyInbox can search text in bouts.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void searchesInBouts() throws Exception {
        final String alias = "alias";
        final Aliases aliases =
            new DyBase().user(new URN("urn:test:88314")).aliases();
        aliases.add(alias);
        final Inbox inbox = aliases.iterate().iterator().next().inbox();
        final Bout first = inbox.bout(inbox.start());
        final Bout second = inbox.bout(inbox.start());
        first.messages().post("hello");
        second.messages().post("world");
        final Iterator<Bout> result = inbox.search("ell").iterator();
        MatcherAssert.assertThat(
            "search result is empty",
            result.hasNext()
        );
        MatcherAssert.assertThat(
            result.next().number(),
            Matchers.equalTo(first.number())
        );
        MatcherAssert.assertThat(
            "more results than expected",
            !result.hasNext()
        );
    }

    /**
     * DyInbox can list bouts and create.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void makesAndListsBouts() throws Exception {
        final String alias = "antony";
        final Aliases aliases =
            new DyBase().user(new URN("urn:test:88")).aliases();
        aliases.add(alias);
        final Inbox inbox = aliases.iterate().iterator().next().inbox();
        final long number = inbox.start();
        MatcherAssert.assertThat(
            inbox.iterate(),
            Matchers.not(Matchers.emptyIterable())
        );
        final Bout bout = inbox.bout(number);
        final Friends friends = bout.friends();
        MatcherAssert.assertThat(
            friends.iterate(),
            Matchers.not(Matchers.emptyIterable())
        );
        MatcherAssert.assertThat(
            friends.iterate(),
            Matchers.hasItem(
                new Friend.HasAlias(Matchers.equalTo(alias))
            )
        );
    }

    /**
     * DyInbox can count unread messages.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void countsUnreadMessages() throws Exception {
        final String alias = "sarah";
        final Aliases aliases =
            new DyBase().user(new URN("urn:test:88026")).aliases();
        aliases.add(alias);
        final Inbox inbox = aliases.iterate().iterator().next().inbox();
        inbox.start();
        MatcherAssert.assertThat(
            inbox.unread(),
            Matchers.equalTo(0L)
        );
    }

    /**
     * DyInbox can jump over the list.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void jumpsOverTheList() throws Exception {
        final String alias = "anthony8";
        final Aliases aliases =
            new DyBase().user(new URN("urn:test:89126656")).aliases();
        aliases.add(alias);
        final Inbox inbox = aliases.iterate().iterator().next().inbox();
        final int total = Tv.FIVE;
        final List<Long> bouts = new ArrayList<Long>(total);
        for (int idx = 0; idx < total; ++idx) {
            bouts.add(inbox.start());
            TimeUnit.MILLISECONDS.sleep(Tv.TEN);
        }
        Collections.reverse(bouts);
        final List<Long> found = new ArrayList<Long>(total);
        Pageable<Bout> pageable = inbox;
        while (true) {
            final Iterator<Bout> iterator = pageable.iterate().iterator();
            if (!iterator.hasNext()) {
                break;
            }
            final Bout bout = iterator.next();
            pageable = pageable.jump(bout.updated().getTime());
            found.add(bout.number());
        }
        MatcherAssert.assertThat(found, Matchers.equalTo(bouts));
    }

}
