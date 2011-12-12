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
import java.net.URL;
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
 * Test case of {@link Hub} and {@link HubMocker}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class HubTest {

    /**
     * HubMocker can mock users in factory.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void mocksUserFactory() throws Exception {
        final String name = "Chuck Norris";
        final User user = new UserMocker().namedAs(name).mock();
        final Hub hub = new HubMocker()
            .withUser(name, user)
            .mock();
        final User found = hub.user(name);
        MatcherAssert.assertThat(found, Matchers.equalTo(user));
    }

    /**
     * DefaultHub produces its statistics as XML element.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void producesStatisticsAsXmlElement() throws Exception {
        final Bus bus = new BusMocker().mock();
        final Hub hub = new DefaultHub(bus);
        final Document doc = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .newDocument();
        doc.appendChild(hub.stats(doc));
        MatcherAssert.assertThat(
            XmlConverters.the(doc),
            XmlMatchers.hasXPath("/catalog")
        );
    }

    /**
     * DefaultHub can promote identity to helper.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void promotesIdentityToHelper() throws Exception {
        final Bus bus = new BusMocker().mock();
        final Hub hub = new DefaultHub(bus);
        final String name = String.valueOf(Math.abs(new Random().nextLong()));
        final User user = hub.user("Billy Bonce");
        final Identity identity = user.identity(name);
        final Helper helper = Mockito.mock(Helper.class);
        Mockito.doReturn(name).when(helper).name();
        Mockito.doReturn(new URL("file:com.netbout")).when(helper).location();
        hub.promote(identity, helper);
        MatcherAssert.assertThat(
            user.identity(name),
            Matchers.equalTo((Identity) helper)
        );
    }

}
