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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.xml.parsers.DocumentBuilderFactory;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.w3c.dom.Document;
import org.xmlmatchers.XmlMatchers;
import org.xmlmatchers.transform.XmlConverters;

/**
 * Test case of {@link DefaultCatalog}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class DefaultCatalogTest {

    /**
     * Catalog produces its statistics as XML element.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void producesStatisticsAsXmlElement() throws Exception {
        final Bus bus = new BusMocker().mock();
        final Catalog catalog = new DefaultCatalog(bus);
        final String name = String.valueOf(Math.abs(new Random().nextLong()));
        catalog.make(name);
        final Document doc = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .newDocument();
        doc.appendChild(catalog.stats(doc));
        MatcherAssert.assertThat(
            XmlConverters.the(doc),
            XmlMatchers.hasXPath(
                String.format("/catalog/identities/identity[.='%s']", name)
            )
        );
    }

    /**
     * Catalog can make an identity just by name.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void makesIdentityWithoutAnyUser() throws Exception {
        final Bus bus = new BusMocker().mock();
        final Catalog catalog = new DefaultCatalog(bus);
        final String name = String.valueOf(Math.abs(new Random().nextLong()));
        final Identity identity = catalog.make(name);
        MatcherAssert.assertThat(identity.name(), Matchers.equalTo(name));
    }

    /**
     * Catalog can convert anonymous identity to the proper one on demand.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void convertsAnonymousIdentityToProperOne() throws Exception {
        final Bus bus = new BusMocker().mock();
        final Catalog catalog = new DefaultCatalog(bus);
        final String name = String.valueOf(Math.abs(new Random().nextLong()));
        final Identity anonymous = catalog.make(name);
        final User user = Mockito.mock(User.class);
        final Identity proper = catalog.make(name, user);
        MatcherAssert.assertThat(anonymous, Matchers.equalTo(proper));
    }

    /**
     * Catalog protects itself from duplicate assignment of the same identity
     * to two different users.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = IllegalArgumentException.class)
    public void avoidsDuplicateAssignmentOfIdentityToUsers() throws Exception {
        final Bus bus = new BusMocker().mock();
        final Catalog catalog = new DefaultCatalog(bus);
        final String name = String.valueOf(Math.abs(new Random().nextLong()));
        final User first = new UserMocker().mock();
        final User second = new UserMocker().mock();
        catalog.make(name, first);
        catalog.make(name, second);
    }

    /**
     * Catalog returns the same identity on similar requests.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void doesntDuplicateIdentities() throws Exception {
        final Bus bus = new BusMocker().mock();
        final Catalog catalog = new DefaultCatalog(bus);
        final String name = String.valueOf(Math.abs(new Random().nextLong()));
        final Identity first = catalog.make(name);
        MatcherAssert.assertThat(catalog.make(name), Matchers.equalTo(first));
    }

    /**
     * Catalog informs bus on every identity being mentioned, just once.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void informsBusAboutIdentityBeingMentioned() throws Exception {
        final Bus bus = new BusMocker().mock();
        final Catalog catalog = new DefaultCatalog(bus);
        final String name = String.valueOf(Math.abs(new Random().nextLong()));
        catalog.make(name);
        catalog.make(name);
        Mockito.verify(bus, Mockito.times(1)).make("identity-mentioned");
    }

    /**
     * Catalog checks identity name and throws exception if it's unreachable.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = com.netbout.spi.UnreachableIdentityException.class)
    public void doesntAllowUnreachableIdentities() throws Exception {
        final Bus bus = new BusMocker().mock();
        final Catalog catalog = new DefaultCatalog(bus);
        final String name = "Bill Gates";
        catalog.make(name);
    }

    /**
     * Catalog checks identity name and throws exception if it's unreachable.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void canBeExtendedByHelpersForReachabilityCheck() throws Exception {
        final Bus bus = new BusMocker()
            .doReturn(true, "can-notify-identity")
            .mock();
        final Catalog catalog = new DefaultCatalog(bus);
        final String name = "funny-name-that-is-reachable";
        catalog.make(name);
    }

    /**
     * Catalog can find identities in pool when they are there.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsIdentitiesByNameWhenTheyExist() throws Exception {
        final Bus bus = new BusMocker()
            // @checkstyle MultipleStringLiterals (1 line)
            .doReturn(new ArrayList<String>(), "find-identities-by-keyword")
            .doReturn(new ArrayList<String>(), "get-aliases-of-identity")
            .mock();
        final Catalog catalog = new DefaultCatalog(bus);
        final String name = String.valueOf(Math.abs(new Random().nextLong()));
        final Identity identity = catalog.make(name);
        MatcherAssert.assertThat(
            catalog.findByKeyword(name),
            Matchers.hasItem(identity)
        );
    }

    /**
     * Catalog can find identities even if they are not in memory, but
     * provided by helper.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsIdentitiesThroughHelper() throws Exception {
        final List<String> names = new ArrayList<String>();
        final String name = String.valueOf(Math.abs(new Random().nextLong()));
        names.add(name);
        final Bus bus = new BusMocker()
            // @checkstyle MultipleStringLiterals (1 line)
            .doReturn(names, "find-identities-by-keyword")
            .mock();
        final Catalog catalog = new DefaultCatalog(bus);
        MatcherAssert.assertThat(
            catalog.findByKeyword(name).iterator().next().name(),
            Matchers.equalTo(name)
        );
    }

}
