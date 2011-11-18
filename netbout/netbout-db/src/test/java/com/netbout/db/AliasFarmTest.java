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
package com.netbout.db;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link AliasFarm}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class AliasFarmTest {

    /**
     * Farm to work with.
     */
    private final AliasFarm farm = new AliasFarm();

    /**
     * Find aliases of some identity.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testAliasFinding() throws Exception {
        final String identity = "William Shakespear";
        final String alias = "willy@example.com";
        new IdentityFarm().identityMentioned(identity);
        this.farm.addedIdentityAlias(identity, alias);
        final String[] aliases = this.farm.getAliasesOfIdentity(identity);
        MatcherAssert.assertThat(aliases, Matchers.hasItemInArray(alias));
    }

    /**
     * Find identity by keyword.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testGlobalFinding() throws Exception {
        final String identity = "Martin Fowler";
        final String alias = "martin@example.com";
        final IdentityFarm ifarm = new IdentityFarm();
        ifarm.identityMentioned(identity);
        this.farm.addedIdentityAlias(identity, alias);
        MatcherAssert.assertThat(
            ifarm.findIdentitiesByKeyword("FOWLER"),
            Matchers.hasItemInArray(identity)
        );
        MatcherAssert.assertThat(
            ifarm.findIdentitiesByKeyword("MARTIN"),
            Matchers.hasItemInArray(identity)
        );
    }

}
