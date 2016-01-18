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
package com.netbout.dynamo;

import com.jcabi.urn.URN;
import com.netbout.spi.Alias;
import com.netbout.spi.Aliases;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Integration case for {@link DyAlias}.
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 */
public final class DyAliasITCase {

    /**
     * DyAlias can make an alias.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void makesAlias() throws Exception {
        final Aliases aliases =
            new DyBase().user(new URN("urn:test:12")).aliases();
        final String name = "walter";
        aliases.add(name);
        MatcherAssert.assertThat(
            aliases.check(name),
            Matchers.not(Matchers.isEmptyOrNullString())
        );
        final Alias alias = aliases.iterate().iterator().next();
        MatcherAssert.assertThat(
            alias,
            new Alias.HasName(Matchers.equalTo(name))
        );
        alias.photo(new URI("http://localhost#test"));
        MatcherAssert.assertThat(
            alias.photo().toString(),
            Matchers.containsString("#test")
        );
    }

    /**
     * DyAlias can reject invalid email.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = Alias.InvalidEmailException.class)
    public void rejectsInvalidEmail() throws Exception {
        final Aliases aliases =
            new DyBase().user(new URN("urn:test:13")).aliases();
        final String name = "max";
        aliases.add(name);
        final Alias alias = aliases.iterate().iterator().next();
        alias.email("test");
    }

    /**
     * DyAlias can accept valid email.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void acceptsValidEmail() throws Exception {
        final Aliases aliases =
            new DyBase().user(new URN("urn:test:14")).aliases();
        final String name = "jack";
        aliases.add(name);
        final Alias alias = aliases.iterate().iterator().next();
        final String[] emails = {
            "test@domain.com", "test@domain.com!test@domain.com",
            "first.second@sub.domain.com", "UpperCase@Mail.com",
        };
        for (final String email: emails) {
            alias.email(email);
            MatcherAssert.assertThat(
                alias.email(),
                Matchers.containsString(email)
            );
        }
    }

}
