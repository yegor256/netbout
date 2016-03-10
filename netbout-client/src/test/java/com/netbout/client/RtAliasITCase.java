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
package com.netbout.client;

import com.netbout.mock.MkBase;
import com.netbout.spi.Alias;
import com.netbout.spi.User;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;

/**
 * Integration case for {@link RtAlias}.
 * @author Matteo Barbieri (barbieri.matteo@gmail.com)
 * @version $Id$
 * @since 2.15
 */
public final class RtAliasITCase {

    /**
     * Netbout rule.
     * @checkstyle VisibilityModifierCheck (3 lines)
     */
    @Rule
    public final transient NbRule rule = new NbRule();

    /**
     * RtAlias can update and retrieve its own mail address.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void updateAndRetrieveEmail() throws Exception {
        final MkBase base = new MkBase();
        final User user = NbRule.get();
        final Alias alias = user.aliases().iterate().iterator().next();
        final String previous = alias.email();
        final int index = previous.indexOf('!');
        final String verified;
        if (index == -1) {
            verified = previous;
        } else {
            verified = previous.substring(0, index);
        }
        final String unverified = base.randomAlias().email();
        alias.email(unverified);
        MatcherAssert.assertThat(
            alias.email(),
            Matchers.is(String.format("%s!%s", verified, unverified))
        );
    }
}
