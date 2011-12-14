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
package com.netbout.db;

import com.netbout.spi.Identity;
import com.netbout.spi.Urn;
import com.netbout.spi.UrnMocker;
import com.rexsl.test.XhtmlConverter;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.mockito.Mockito;
import org.xmlmatchers.XmlMatchers;
import org.xmlmatchers.namespace.SimpleNamespaceContext;

/**
 * Test case of {@link StatsFarm}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class StatsFarmTest {

    /**
     * Farm to work with.
     */
    private final transient StatsFarm farm = new StatsFarm();

    /**
     * Find aliases of some identity.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testSummaryRendering() throws Exception {
        final Identity identity = Mockito.mock(Identity.class);
        Mockito.doReturn(new UrnMocker().mock()).when(identity).name();
        this.farm.init(identity);
        final String xml = this.farm.renderStageXml(1L, identity.name(), "");
        MatcherAssert.assertThat(
            XhtmlConverter.the(xml),
            XmlMatchers.hasXPath("/data/summary")
        );
    }

    /**
     * Render XSL.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testRenderingOfXslStylesheet() throws Exception {
        final Identity identity = Mockito.mock(Identity.class);
        Mockito.doReturn(new UrnMocker().mock()).when(identity).name();
        this.farm.init(identity);
        final String xsl = this.farm.renderStageXsl(1L, identity.name());
        MatcherAssert.assertThat(
            XhtmlConverter.the(xsl),
            XmlMatchers.hasXPath(
                "/xsl:stylesheet",
                new SimpleNamespaceContext()
                    .withBinding("xsl", "http://www.w3.org/1999/XSL/Transform")
            )
        );
    }

}
