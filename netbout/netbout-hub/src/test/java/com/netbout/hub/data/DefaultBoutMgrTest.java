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
package com.netbout.hub.data;

import com.netbout.bus.Bus;
import com.netbout.bus.BusMocker;
import com.netbout.bus.DefaultBus;
import com.netbout.hub.BoutMgr;
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
 * Test case of {@link DefaultBoutMgr}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class DefaultBoutMgrTest {

    /**
     * DefaultBoutMgr can produce stats in XML.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void producesStatisticsAsXmlElement() throws Exception {
        final Bus bus = new BusMocker().mock();
        final BoutMgr mgr = new DefaultBoutMgr(bus);
        final Document doc = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .newDocument();
        doc.appendChild(mgr.stats(doc));
        MatcherAssert.assertThat(
            XmlConverters.the(doc),
            XmlMatchers.hasXPath("/manager/total")
        );
    }

    /**
     * DefaultBoutMgr can create new bout.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void createsNewBout() throws Exception {
        final Long number = new Random().nextLong();
        final Bus bus = new BusMocker()
            .doReturn(number, "get-next-bout-number")
            .mock();
        final BoutMgr mgr = new DefaultBoutMgr(bus);
        final Long num = mgr.create();
        MatcherAssert.assertThat(num, Matchers.equalTo(number));
    }

    /**
     * DefaultBoutMgr can create new bout on top of real bus.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void createsNewBoutWithRealBus() throws Exception {
        final BoutMgr mgr = new DefaultBoutMgr(new DefaultBus());
        final Long first = mgr.create();
        final Long second = mgr.create();
        MatcherAssert.assertThat(first, Matchers.not(Matchers.equalTo(second)));
    }

}
