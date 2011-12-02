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

import com.netbout.spi.Identity;
import java.net.URL;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case of {@link User}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class UserTest {

    /**
     * Two objects of class User should match each other by name only.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void matchesWithOtherUsersByNameSimilarityOnly() throws Exception {
        final String name = "Big Lebowski";
        final Catalog catalog = Mockito.mock(Catalog.class);
        final User userA = new User(catalog, name);
        final User userB = new User(catalog, name);
        MatcherAssert.assertThat(userA, Matchers.equalTo(userB));
        MatcherAssert.assertThat(userA.equals(name), Matchers.is(false));
        MatcherAssert.assertThat(
            userA.hashCode(),
            Matchers.equalTo(userB.hashCode())
        );
    }

    /**
     * Identity can be found in a user by its name, and it will be retrieved
     * from a catalog.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsIdentitiesByNameInCatalog() throws Exception {
        final String name = "Jeff Bridges";
        final Identity identity = Mockito.mock(Identity.class);
        final Catalog catalog = Mockito.mock(Catalog.class);
        final User user = new User(catalog, "jeff");
        Mockito.doReturn(identity).when(catalog).make(name, user);
        final Identity found = user.identity(name);
        MatcherAssert.assertThat(found, Matchers.equalTo(identity));
    }

}
