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
 * incident to the author by email: privacy@netbout.com.
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
package com.netbout.war;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.xmlmatchers.transform.XmlConverters;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@RunWith(Parameterized.class)
public final class XslTransformationTest {

    /**
     * Full list of pages to test.
     */
    private static final String[] PAGES = {
        "PageWithBouts",
    };

    private static final URIResolver RESOLVER =
        new XslTransformationTest.ClassPathResolver();

    private static final TransformerFactory FACTORY =
        TransformerFactory.newInstance();

    private final String page;

    public XslTransformationTest(final String name) {
        this.page = name;
        this.FACTORY.setURIResolver(this.RESOLVER);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> pages() {
        final Collection<Object[]> pages = new ArrayList<Object[]>();
        for (String name : XslTransformationTest.PAGES) {
            pages.add(new Object[] {name});
        }
        return pages;
    }

    @Test
    public void testOnePageRendering() throws Exception {
        final Source xsl = this.RESOLVER.resolve(
            "/xsl/" + this.page + ".xsl", null);
        final Source xml = new StreamSource(
            this.getClass().getResourceAsStream(
                "/com/netbout/war/XslTransformationTest/" + this.page + ".xml"
            )
        );
        final Transformer transformer = this.FACTORY.newTransformer(xsl);
        final StringWriter writer = new StringWriter();
        transformer.transform(xml, new StreamResult(writer));
        final String xhtml = writer.toString();
        assertThat(
            XmlConverters.the(xhtml),
            org.xmlmatchers.XmlMatchers.hasXPath("/html")
        );
    }

    private static final class ClassPathResolver implements URIResolver {
        @Override
        public Source resolve(final String href, final String base) {
            return new StreamSource(new File("./src/main/webapp" + href));
        }
    }

}
