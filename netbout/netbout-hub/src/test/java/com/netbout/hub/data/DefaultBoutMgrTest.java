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
import com.netbout.hub.BoutMgr;
import com.netbout.hub.DefaultHub;
import com.netbout.hub.Hub;
import com.netbout.hub.HubMocker;
import com.netbout.spi.xml.JaxbPrinter;
import com.rexsl.test.XhtmlConverter;
import com.rexsl.test.XhtmlMatchers;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

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
        MatcherAssert.assertThat(
            XhtmlConverter.the(
                new JaxbPrinter(
                    new DefaultBoutMgr(new HubMocker().mock())
                ).print()
            ),
            XhtmlMatchers.hasXPath("/manager/bouts")
        );
    }

    /**
     * DefaultBoutMgr can create new bout.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void createsNewBout() throws Exception {
        final Long number = new Random().nextLong();
        final Hub hub = new HubMocker()
            .doReturn(number, "get-next-bout-number")
            .mock();
        final BoutMgr mgr = new DefaultBoutMgr(hub);
        final Long num = mgr.create();
        MatcherAssert.assertThat(num, Matchers.equalTo(number));
    }

    /**
     * DefaultBoutMgr can create new bout on top of real hub.
     * @throws Exception If there is some problem inside
     */
    @Test
    @org.junit.Ignore
    public void createsNewBoutWithRealHub() throws Exception {
        final Bus bus = new BusMocker().mock();
        final BoutMgr mgr = new DefaultBoutMgr(new DefaultHub(bus));
        final Long first = mgr.create();
        final Long second = mgr.create();
        MatcherAssert.assertThat(first, Matchers.not(Matchers.equalTo(second)));
    }

}
