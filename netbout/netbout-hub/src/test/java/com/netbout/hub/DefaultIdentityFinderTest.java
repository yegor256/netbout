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

import com.netbout.bus.Bus;
import com.netbout.bus.BusMocker;
import com.netbout.spi.Identity;
import com.netbout.spi.IdentityMocker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case of {@link DefaultIdentityFinder}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class DefaultIdentityFinderTest {

    /**
     * IdentityFinder can find identity that already exists in collection.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsIdentityAmongExistingCollection() throws Exception {
        final Catalog catalog = new DefaultCatalog(this.bus());
        final NameValidator validator = Mockito.mock(NameValidator.class);
        final ConcurrentMap<String, Identity> existing =
            new ConcurrentHashMap<String, Identity>();
        final String name = "foo";
        existing.put(name, new IdentityMocker().namedAs(name).mock());
        final IdentityFinder finder = new DefaultIdentityFinder(
            catalog, this.bus(), existing, validator
        );
        final Set<Identity> found = finder.find(name);
        MatcherAssert.assertThat(
            "We found exactly one identity, which existed in the provided Map",
            found.size(),
            Matchers.equalTo(1)
        );
    }

    /**
     * IdentityFinder can find identity that is already assigned to a user.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsIdentityAlreadyAssignedToSomeUser() throws Exception {
        final Catalog catalog = new DefaultCatalog(this.bus());
        final NameValidator validator = Mockito.mock(NameValidator.class);
        Mockito.doReturn(true).when(validator).isValid(Mockito.anyString());
        final ConcurrentMap<String, Identity> existing =
            new ConcurrentHashMap<String, Identity>();
        final String name = "nb:some-name";
        final User user = new UserMocker().mock();
        existing.put(name, catalog.make(name, user));
        final IdentityFinder finder = new DefaultIdentityFinder(
            catalog, this.bus(), existing, validator
        );
        final Set<Identity> found = finder.find(name);
        MatcherAssert.assertThat(
            "We found exactly one identity, which existed in the Map",
            found.size(),
            Matchers.equalTo(1)
        );
    }

    /**
     * IdentityFinder can find in existing map first, before going to helper.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsInExistingMapFirst() throws Exception {
        final String name = "736547383";
        final Catalog catalog = new DefaultCatalog(this.bus(name));
        final ConcurrentMap<String, Identity> existing =
            new ConcurrentHashMap<String, Identity>();
        final String uname = "Jeff";
        final User user = new UserMocker().namedAs(uname).mock();
        existing.put(name, catalog.make(name, user));
        final NameValidator validator = Mockito.mock(NameValidator.class);
        final IdentityFinder finder = new DefaultIdentityFinder(
            catalog, this.bus(name), existing, validator
        );
        final Set<Identity> found = finder.find(name);
        MatcherAssert.assertThat(
            "We found exactly one identity, from the Map",
            found.iterator().next().user(),
            Matchers.equalTo(uname)
        );
    }

    /**
     * Create bus.
     * @param names Names of identities
     * @return The bus just mocked
     * @throws Exception If there is some problem inside
     */
    private Bus bus(final String... names) throws Exception {
        final BusMocker mocker = new BusMocker();
        mocker.doReturn(Arrays.asList(names), "find-identities-by-keyword");
        mocker.doReturn(new ArrayList<String>(), "get-aliases-of-identity");
        return mocker.mock();
    }

}
