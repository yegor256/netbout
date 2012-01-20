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
package com.netbout.shary;

import com.netbout.spi.Bout;
import com.netbout.spi.BoutMocker;
import com.netbout.spi.Identity;
import com.netbout.spi.IdentityMocker;
import com.netbout.spi.Urn;
import com.netbout.spi.UrnMocker;
import com.rexsl.test.XhtmlConverter;
import com.rexsl.test.XhtmlMatchers;
import com.woquo.netbout.Jaxb;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * Test case of {@link StageFarm}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class StageFarmTest {

    /**
     * Farm renders stage XML.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void rendersStageXml() throws Exception {
        final StageFarm farm = new StageFarm();
        final Urn name = new UrnMocker().mock();
        final Slip slip = new Slip(true, "s3:...", "John");
        final Bout bout = new BoutMocker()
            .withMessage(Jaxb.format(slip))
            .mock();
        final Identity identity = new IdentityMocker()
            .namedAs(name)
            .withBout(1L, bout)
            .mock();
        farm.init(identity);
        // final String xml = farm.renderStageXml(1L, name, "");
        // MatcherAssert.assertThat(
        //     XhtmlConverter.the(xml),
        //     Matchers.allOf(
        //         XhtmlMatchers.hasXPath("/data/docs/doc")
        //     )
        // );
    }

    /**
     * Render XSL.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testRenderingOfXslStylesheet() throws Exception {
        final StageFarm farm = new StageFarm();
        final Identity identity = new IdentityMocker().mock();
        farm.init(identity);
        final String xsl = farm.renderStageXsl(1L, identity.name());
        MatcherAssert.assertThat(
            XhtmlConverter.the(xsl),
            XhtmlMatchers.hasXPath("/xsl:stylesheet")
        );
    }

}
