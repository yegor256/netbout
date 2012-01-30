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
package com.netbout.notifiers.email;

import com.netbout.spi.Bout;
import com.netbout.spi.BoutMocker;
import com.netbout.spi.Identity;
import com.netbout.spi.IdentityMocker;
import com.netbout.spi.Urn;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link EmailFarm}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class EmailFarmTest {

    /**
     * EmailFarm can send notify bout participants.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void notfiesBoutParticipants() throws Exception {
        final Identity identity = new IdentityMocker().mock();
        final Identity receiver = new IdentityMocker()
            .namedAs("urn:email:yegor%40tpc2%2Ecom")
            .mock();
        final Bout bout = new BoutMocker()
            .withParticipant(receiver)
            .titledAs("some bout title")
            .mock();
        Mockito.doReturn(bout).when(identity).bout(Mockito.anyLong());
        final EmailFarm farm = new EmailFarm();
        farm.init(identity);
        farm.notifyBoutParticipants(bout.number(), 0L);
    }

    /**
     * EmailFarm can construct a URN from email.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void constructsUrnFromEmail() throws Exception {
        final EmailFarm farm = new EmailFarm();
        final List<Urn> urns = farm.findIdentitiesByKeyword("abc@a.com");
        MatcherAssert.assertThat(urns, Matchers.hasSize(1));
        MatcherAssert.assertThat(
            urns.get(0),
            Matchers.equalTo(new Urn("urn:email:abc%40a%2Ecom"))
        );
    }

    /**
     * EmailFarm can ignore non-email URNs.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void ingoresOtherKeywords() throws Exception {
        MatcherAssert.assertThat(
            new EmailFarm().findIdentitiesByKeyword("some text"),
            Matchers.nullValue()
        );
    }

}
