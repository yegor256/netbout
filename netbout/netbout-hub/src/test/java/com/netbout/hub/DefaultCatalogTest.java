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
import com.netbout.spi.Identity;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
        final Document doc = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .newDocument();
        doc.appendChild(catalog.stats(doc));
        MatcherAssert.assertThat(
            XmlConverters.the(doc),
            XmlMatchers.hasXPath("/catalog/identities")
        );
    }

    // /**
    //  * Make reachable identity.
    //  * @throws Exception If there is some problem inside
    //  */
    // @Test
    // public void makesReachableIdentity() throws Exception {
    //     final String name = "66632";
    //     final Identity reachable = catalog.make(name);
    //     MatcherAssert.assertThat(reachable.name(), Matchers.equalTo(name));
    // }
    //
    // /**
    //  * Make un-reachable identities.
    //  * @throws Exception If there is some problem inside
    //  */
    // @Test(expected = IllegalArgumentException.class)
    // public void testMakingOfUnReachableIdentities() throws Exception {
    //     Identities.make("john");
    // }

}
