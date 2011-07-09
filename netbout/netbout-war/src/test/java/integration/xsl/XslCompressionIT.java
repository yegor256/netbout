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
package integration.xsl;

import com.jayway.restassured.RestAssured;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.xmlmatchers.transform.XmlConverters;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Here we test that XSL compression really works, and XML comments
 * are not visible to public.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @todo #107 This test doesn't work now because we don't compress
 *       XSL files during WAR packaging. This functionality has to
 *       be implemented in pom.xml. I don't know what Maven plugin
 *       should be used for this, maybe a plain simple Groovy script
 *       through org.codehaus.gmaven:gmaven-plugin.
 */
@Ignore
@RunWith(Parameterized.class)
public final class XslCompressionIT {

    /**
     * Full list of URLs to test.
     */
    private static final String[] URLS = {
        "/xsl/layout.xsl",
        "/xsl/PageWithBouts.xsl",
    };

    private final String path;

    public XslCompressionIT(final String name) {
        this.path = name;
    }

    @BeforeClass
    public static void configureRestAssured() {
        RestAssured.port = Integer.valueOf(System.getProperty("jetty.port"));
    }

    @Parameterized.Parameters
    public static Collection<Object[]> paths() {
        final Collection<Object[]> paths = new ArrayList<Object[]>();
        for (String url : XslCompressionIT.URLS) {
            paths.add(new Object[] {url});
        }
        return paths;
    }

    @Test
    public void testOnePageRendering() throws Exception {
        final String xsl = RestAssured.get(this.path).asString();
        assertThat(
            XmlConverters.the(xsl),
            not(org.xmlmatchers.XmlMatchers.hasXPath("//comment()"))
        );
    }

}
