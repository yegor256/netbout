/**
 * Copyright (c) 2009-2011, netBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code occasionally and without intent to use it, please report this
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
package com.netbout.hub;

import com.netbout.spi.Bout;
import com.netbout.spi.BoutMocker;
import com.netbout.spi.Identity;
import com.netbout.spi.UrnMocker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link HubIdentity}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class HubIdentityTest {

    /**
     * HubIdentity can sort bouts before returning them back.
     * @throws Exception If there is some problem inside
     * @todo #169 Doesn't work at the moment because Bus is not complete now
     */
    @Test
    @org.junit.Ignore
    public void sortsBoutsByRecentlyPostedMessages() throws Exception {
        final List<Long> nums = new ArrayList<Long>();
        final Bout first = new BoutMocker().mock();
        nums.add(first.number());
        final Bout second = new BoutMocker().mock();
        nums.add(second.number());
        final Bout third = new BoutMocker().mock();
        nums.add(third.number());
        final Hub hub = new HubMocker()
            .doReturn(nums, "get-bouts-of-identity")
            .doReturn(
                Arrays.asList(new Long[]{1L}),
                "get-bout-messages",
                second.number()
            )
            .mock();
        final Identity identity = new HubIdentity(hub, new UrnMocker().mock());
        MatcherAssert.assertThat(
            identity.inbox("").get(0).number(),
            Matchers.equalTo(second.number())
        );
    }

    /**
     * HubIdentity can find bouts by predicate, even without messages.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsBoutsWithoutMessages() throws Exception {
        final List<Long> nums = new ArrayList<Long>();
        final Bout bout = new BoutMocker().mock();
        nums.add(bout.number());
        final Hub hub = new HubMocker()
            .doReturn(nums, "get-bouts-of-identity")
            .doReturn(new ArrayList<Long>(), "get-bout-messages")
            .mock();
        final Identity identity = new HubIdentity(hub, new UrnMocker().mock());
        MatcherAssert.assertThat(
            identity.inbox("(matches '' $text)").size(),
            Matchers.equalTo(1)
        );
    }

}
