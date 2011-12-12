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
package com.netbout.rest;

import com.netbout.bus.Bus;
import com.netbout.bus.BusMocker;
import com.netbout.hub.Hub;
import com.netbout.hub.HubMocker;
import com.netbout.hub.User;
import com.netbout.hub.UserMocker;
import com.netbout.spi.Bout;
import com.netbout.spi.BoutMocker;
import com.netbout.spi.Identity;
import com.netbout.spi.IdentityMocker;
import com.rexsl.test.XhtmlConverter;
import java.net.URLEncoder;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xmlmatchers.XmlMatchers;
import org.xmlmatchers.namespace.SimpleNamespaceContext;

/**
 * Test case for {@link BoutStylesheetRs}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class BoutStylesheetRsTest {

    /**
     * XPath context.
     */
    private static final SimpleNamespaceContext CONTEXT =
        new SimpleNamespaceContext().withBinding(
            "xsl",
            "http://www.w3.org/1999/XSL/Transform"
        );

    /**
     * XSL wrapper is renderable.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testWrappingXslRendering() throws Exception {
        final Bout bout = new BoutMocker().mock();
        final Identity identity = new IdentityMocker()
            .withBout(bout.number(), bout)
            .mock();
        final BoutStylesheetRs rest = new ResourceMocker()
            .withIdentity(identity)
            .mock(BoutStylesheetRs.class);
        final String stage = "R&D stage name";
        rest.setBout(bout.number());
        rest.setStage(stage);
        final String xsl = rest.boutXsl();
        MatcherAssert.assertThat(
            XhtmlConverter.the(xsl),
            XmlMatchers.hasXPath(
                "/xsl:stylesheet/xsl:include[contains(@href,'/xsl/bout.xsl')]",
                this.CONTEXT
            )
        );
        final String xpath = String.format(
            // @checkstyle LineLength (1 line)
            "//xsl:include[contains(@href,'/%d/xsl/stage.xsl') and contains(@href,'stage=%s')]",
            bout.number(),
            URLEncoder.encode(stage, "UTF-8")
        );
        MatcherAssert.assertThat(
            XhtmlConverter.the(xsl),
            XmlMatchers.hasXPath(xpath, this.CONTEXT)
        );
    }

    /**
     * Stage-related XSL is renderable.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testStageXslRendering() throws Exception {
        final Bout bout = new BoutMocker().mock();
        final String uname = "Steven";
        final Identity identity = new IdentityMocker()
            .withBout(bout.number(), bout)
            .belongsTo(uname)
            .mock();
        final User user = new UserMocker()
            .namedAs(uname)
            .withIdentity(identity.name(), identity)
            .mock();
        final Hub hub = new HubMocker()
            .withUser(user.name(), user)
            .mock();
        final String text = "some text in XSL format";
        final Bus bus = new BusMocker()
            .doReturn(text, "render-stage-xsl")
            .mock();
        final BoutStylesheetRs rest = new ResourceMocker()
            .withIdentity(identity)
            .withDeps(bus, hub)
            .mock(BoutStylesheetRs.class);
        final String stage = "nb:hh";
        rest.setBout(bout.number());
        rest.setStage(stage);
        final String xsl = rest.stageXsl();
        MatcherAssert.assertThat(xsl, Matchers.equalTo(text));
    }

}
