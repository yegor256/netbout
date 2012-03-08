/**
 * Copyright (c) 2009-2011, NetBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi;

import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Identity} and {@link IdentityMocker}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class IdentityTest {

    /**
     * IdentityMocker can assign name to identity.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void canHaveANameMocked() throws Exception {
        final Urn name = new UrnMocker().mock();
        final Identity identity = new IdentityMocker()
            .namedAs(name.toString()).mock();
        MatcherAssert.assertThat(identity.name(), Matchers.equalTo(name));
    }

    /**
     * IdentityMocker can assign an alias to identity.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void canHaveAnAliasMocked() throws Exception {
        final String alias = "some alias";
        final Identity identity = new IdentityMocker()
            .withProfile(new ProfileMocker().withAlias(alias).mock())
            .mock();
        MatcherAssert.assertThat(
            NetboutUtils.aliasOf(identity),
            Matchers.equalTo(alias)
        );
    }

    /**
     * IdentityMocker can assign user to identity.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void canBelongToSomeMockedUser() throws Exception {
        final String uname = "http://localhost/auth";
        final Identity identity = new IdentityMocker().belongsTo(uname).mock();
        MatcherAssert.assertThat(
            identity.authority().toString(),
            Matchers.equalTo(uname)
        );
    }

    /**
     * IdentityMocker can set properties by default.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void setsAllIdentityPropertiesByDefault() throws Exception {
        final Identity identity = new IdentityMocker().mock();
        MatcherAssert.assertThat(identity.name(), Matchers.notNullValue());
        MatcherAssert.assertThat(identity.authority(), Matchers.notNullValue());
    }

    /**
     * IdentityMocker can start new bout.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void startsBoutByDefault() throws Exception {
        final Identity identity = new IdentityMocker().mock();
        final Bout bout = identity.start();
        MatcherAssert.assertThat(bout, Matchers.notNullValue());
        MatcherAssert.assertThat(
            identity.bout(bout.number()),
            Matchers.notNullValue()
        );
        MatcherAssert.assertThat(
            identity.inbox(""),
            Matchers.<Bout>iterableWithSize(2)
        );
    }

    /**
     * IdentityMocker can add bout.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void addsBoutByNumber() throws Exception {
        final Long number = Math.abs(new Random().nextLong());
        final Bout bout = new BoutMocker().mock();
        final Identity identity = new IdentityMocker()
            .withBout(number, bout)
            .mock();
        MatcherAssert.assertThat(identity.bout(number), Matchers.equalTo(bout));
    }

    /**
     * IdentityMocker can mock inbox response.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void mocksDifferentInboxQueries() throws Exception {
        final String query = "some query";
        final Identity identity = new IdentityMocker()
            .withBout(1L, new BoutMocker().mock())
            .withBout(2L, new BoutMocker().mock())
            .withInbox(query, new Long[] {1L})
            .mock();
        MatcherAssert.assertThat(
            identity.inbox(query),
            Matchers.<Bout>iterableWithSize(1)
        );
        MatcherAssert.assertThat(
            identity.inbox(""),
            Matchers.<Bout>iterableWithSize(2)
        );
    }

    /**
     * IdentityMocker can sort bouts by number.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void sortsBoutByNumber() throws Exception {
        final Long num = Math.abs(new Random().nextLong());
        final Identity identity = new IdentityMocker()
            .withBout(num, new BoutMocker().withNumber(num).mock())
            .withBout(num + 1, new BoutMocker().withNumber(num + 1).mock())
            .withBout(num - 1, new BoutMocker().withNumber(num - 1).mock())
            .mock();
        MatcherAssert.assertThat(
            identity.inbox("").iterator().next().number(),
            Matchers.equalTo(num + 1)
        );
    }

}
