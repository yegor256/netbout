/**
 * Copyright (c) 2009-2014, Netbout.com
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
package com.netbout.rest;

import com.jcabi.urn.URN;
import com.jcabi.urn.URNMocker;
import com.netbout.hub.Hub;
import com.netbout.spi.Identity;
import com.netbout.spi.IdentityMocker;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link CryptedIdentity}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class CryptedIdentityTest {

    /**
     * CryptedIdentity can encrypt identity and decrypt.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void encryptsAndDecryptsIdentity() throws Exception {
        final URN iname = new URNMocker().mock();
        final Identity identity = new IdentityMocker()
            .namedAs(iname)
            .mock();
        final String hash = new CryptedIdentity(identity).toString();
        final Hub hub = Mockito.mock(Hub.class);
        Mockito.doReturn(identity).when(hub).identity(iname);
        final Identity discovered = CryptedIdentity.parse(hub, hash);
        MatcherAssert.assertThat(discovered, Matchers.equalTo(identity));
        Mockito.verify(hub).identity(iname);
    }

    /**
     * CryptedIdentity produces HASH with only alphabetic chars inside.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void hashDoestHaveIllegalCharacters() throws Exception {
        final Identity identity = new IdentityMocker()
            .namedAs("urn:foo:hello%40example%2Ecom")
            .mock();
        final String hash = new CryptedIdentity(identity).toString();
        MatcherAssert.assertThat(
            hash.matches("^[\\w=\\+\\./]+$"),
            Matchers.describedAs(hash, Matchers.is(true))
        );
    }

}
