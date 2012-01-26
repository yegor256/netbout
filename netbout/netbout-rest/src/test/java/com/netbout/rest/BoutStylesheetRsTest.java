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

import com.netbout.hub.Hub;
import com.netbout.hub.HubMocker;
import com.netbout.spi.Bout;
import com.netbout.spi.BoutMocker;
import com.netbout.spi.Identity;
import com.netbout.spi.IdentityMocker;
import com.netbout.spi.Urn;
import com.netbout.spi.UrnMocker;
import com.rexsl.test.XhtmlConverter;
import com.rexsl.test.XhtmlMatchers;
import javax.ws.rs.core.UriBuilder;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link BoutStylesheetRs}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class BoutStylesheetRsTest {

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
        final Urn stage = new UrnMocker().mock();
        rest.setBout(bout.number());
        rest.setStage(stage);
        final String xsl = rest.boutXsl();
        MatcherAssert.assertThat(
            XhtmlConverter.the(xsl),
            XhtmlMatchers.hasXPath(
                "/xsl:stylesheet/xsl:include[contains(@href,'/xsl/bout.xsl')]"
            )
        );
        final String xpath = String.format(
            "//xsl:include[contains(@href,'%s')]",
            UriBuilder.fromPath("/{bout}/xsl/{stage}/stage.xsl")
                .build(bout.number(), stage)
        );
        MatcherAssert.assertThat(
            XhtmlConverter.the(xsl),
            XhtmlMatchers.hasXPath(xpath)
        );
    }

    /**
     * Stage-related XSL is renderable.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testStageXslRendering() throws Exception {
        final Bout bout = new BoutMocker().mock();
        final Identity identity = new IdentityMocker()
            .withBout(bout.number(), bout)
            .mock();
        final String text = "some text in XSL format";
        final Hub hub = new HubMocker()
            .withIdentity(identity.name(), identity)
            .doReturn(text, "render-stage-xsl")
            .mock();
        final BoutStylesheetRs rest = new ResourceMocker()
            .withIdentity(identity)
            .withHub(hub)
            .mock(BoutStylesheetRs.class);
        final Urn stage = new UrnMocker().mock();
        rest.setBout(bout.number());
        rest.setStage(stage);
        final String xsl = rest.stageXsl();
        MatcherAssert.assertThat(xsl, Matchers.equalTo(text));
    }

}
