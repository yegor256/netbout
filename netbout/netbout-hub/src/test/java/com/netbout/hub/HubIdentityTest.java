/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
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
package com.netbout.hub;

import com.netbout.inf.Infinity;
import com.netbout.spi.Query;
import com.netbout.spi.Urn;
import com.netbout.spi.UrnMocker;
import java.util.Arrays;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case of {@link HubIdentity}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class HubIdentityTest {

    /**
     * HubIdentity can create proper predicate for infinity.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void createsProperRequestForInfinity() throws Exception {
        final PowerHub hub = Mockito.mock(PowerHub.class);
        final Infinity infinity = Mockito.mock(Infinity.class);
        Mockito.doReturn(infinity).when(hub).infinity();
        final Urn name = new UrnMocker().mock();
        new HubIdentity(hub, name).inbox(new Query.Textual(""));
        Mockito.verify(infinity).messages(
            Mockito.<Query>argThat(
                Matchers.<Query>hasToString(
                    Matchers.containsString(
                        String.format("(talks-with '%s')", name)
                    )
                )
            )
        );
    }

    /**
     * HubIdentity can return plain aliases first.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void returnsPlainAliasesFirst() throws Exception {
        final String clean = "John Doe";
        final String[] names = new String[] {"b@b.com", clean, "a@a.com"};
        final PowerHub hub = new PowerHubMocker().doReturn(
            Arrays.asList(names),
            "get-aliases-of-identity"
        )
            .mock();
        final Urn name = new UrnMocker().mock();
        final Set<String> aliases = new HubIdentity(hub, name)
            .profile()
            .aliases();
        MatcherAssert.assertThat(aliases, Matchers.hasSize(names.length));
        MatcherAssert.assertThat(
            aliases.iterator().next(),
            Matchers.equalTo(clean)
        );
    }

}
