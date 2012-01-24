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
import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import com.netbout.spi.IdentityMocker;
import com.netbout.spi.Urn;
import com.netbout.spi.UrnMocker;
import com.netbout.spi.xml.JaxbPrinter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.xmlmatchers.XmlMatchers;
import org.xmlmatchers.transform.XmlConverters;

/**
 * Test case of {@link DefaultHub}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle MultipleStringLiterals (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class DefaultHubTest {

    /**
     * DefaultHub can create an identity by name.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void createsIdentityByName() throws Exception {
        final Urn name = new UrnMocker().mock();
        final Bus bus = new BusMocker()
            .doReturn(new ArrayList<String>(), "get-all-namespaces")
            .mock();
        final Hub hub = new DefaultHub(bus);
        hub.resolver().register(
            new IdentityMocker().mock(), name.nid(), "http://abc"
        );
        final Identity identity = hub.identity(name);
        MatcherAssert.assertThat(identity.name(), Matchers.equalTo(name));
    }

    /**
     * DefaultHub produces its statistics as XML element.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void producesStatisticsAsXmlElement() throws Exception {
        final Bus bus = new BusMocker()
            .doReturn(new ArrayList<String>(), "get-all-namespaces")
            .mock();
        MatcherAssert.assertThat(
            XmlConverters.the(new JaxbPrinter(new DefaultHub(bus)).print()),
            XmlMatchers.hasXPath("/hub")
        );
    }

    /**
     * DefaultHub can promote identity to helper.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void promotesIdentityToHelper() throws Exception {
        final Urn name = new UrnMocker().mock();
        final Bus bus = new BusMocker()
            .doReturn(new ArrayList<String>(), "get-all-namespaces")
            .mock();
        final Hub hub = new DefaultHub(bus);
        hub.resolver().register(
            new IdentityMocker().mock(), name.nid(), "http://cde"
        );
        final Identity identity = hub.identity(name);
        final Helper helper = Mockito.mock(Helper.class);
        Mockito.doReturn(new URL("file:com.netbout")).when(helper).location();
        Mockito.doReturn(name).when(helper).name();
        hub.promote(identity, helper);
        MatcherAssert.assertThat(
            hub.identity(name),
            Matchers.equalTo((Identity) helper)
        );
    }

    /**
     * Hub can return the same identity on similar requests.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void doesntDuplicateIdentities() throws Exception {
        final Bus bus = new BusMocker()
            .doReturn(new ArrayList<String>(), "get-all-namespaces")
            .mock();
        final Hub hub = new DefaultHub(bus);
        final Urn name = new UrnMocker().mock();
        hub.resolver().register(
            new IdentityMocker().mock(), name.nid(), "http://foo"
        );
        final Identity first = hub.identity(name);
        MatcherAssert.assertThat(hub.identity(name), Matchers.equalTo(first));
    }

    /**
     * Catalog can inform Bus on every identity being mentioned, just once.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void informsBusAboutIdentityBeingMentioned() throws Exception {
        final Bus bus = new BusMocker()
            .doReturn(new ArrayList<String>(), "get-all-namespaces")
            .mock();
        final Hub hub = new DefaultHub(bus);
        final Urn name = new UrnMocker().mock();
        hub.resolver().register(
            new IdentityMocker().mock(), name.nid(), "http://bar"
        );
        hub.identity(name);
        hub.identity(name);
        Mockito.verify(bus, Mockito.times(1)).make("identity-mentioned");
    }

    /**
     * Catalog can check identity name and throws exception if it's unreachable.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = com.netbout.spi.UnreachableUrnException.class)
    public void doesntAllowUnreachableIdentities() throws Exception {
        final Bus bus = new BusMocker()
            .doReturn(new ArrayList<String>(), "get-all-namespaces")
            .mock();
        final Hub hub = new DefaultHub(bus);
        final Urn name = new UrnMocker().mock();
        hub.identity(name);
    }

    /**
     * DefaultHub can find identities in pool when they are there.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsIdentitiesByNameWhenTheyExist() throws Exception {
        final Urn name = new UrnMocker().mock();
        final List<Urn> names = new ArrayList<Urn>();
        names.add(name);
        final Bus bus = new BusMocker()
            // @checkstyle MultipleStringLiterals (1 line)
            .doReturn(names, "find-identities-by-keyword")
            .doReturn(new ArrayList<String>(), "get-all-namespaces")
            .doReturn(new ArrayList<String>(), "get-aliases-of-identity")
            .mock();
        final Hub hub = new DefaultHub(bus);
        hub.resolver().register(
            new IdentityMocker().mock(), name.nid(), "http://foo-foo"
        );
        final Identity identity = hub.identity(name);
        MatcherAssert.assertThat(
            hub.findByKeyword(name.nss()),
            Matchers.hasItem(identity)
        );
    }

}
