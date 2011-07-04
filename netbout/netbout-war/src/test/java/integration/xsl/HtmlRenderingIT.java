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

import integration.ContainerPage;
import integration.ContainerURL;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.xmlmatchers.transform.XmlConverters;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@RunWith(Parameterized.class)
public final class HtmlRenderingIT {

    /**
     * Full list of URLs to test.
     */
    private static final String[] URLS = {
        "/"
    };

    private final String path;

    private final TransformerFactory factory = TransformerFactory.newInstance();

    @Rule
    public TemporaryFolder root = new TemporaryFolder();

    private File folder;

    private URIResolver resolver;

    public HtmlRenderingIT(final String name) {
        this.path = name;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> paths() {
        final Collection<Object[]> paths = new ArrayList<Object[]>();
        for (String url : HtmlRenderingIT.URLS) {
            paths.add(new Object[] {url});
        }
        return paths;
    }

    @Before
    public void prepare() throws Exception {
        assertThat(this.root.getRoot(), is(not(nullValue())));
        this.folder = this.root.newFolder("xsl");
        this.resolver = new HtmlRenderingIT.InDirResolver(this.folder);
        this.factory.setURIResolver(this.resolver);
    }

    @Test
    public void testOnePageRendering() throws Exception {
        final File page = new File(this.folder, "page.xml");
        FileUtils.writeStringToFile(
            page,
            new ContainerPage().xml(this.path)
        );
        final Source xml = new StreamSource(page);
        final Source xsl = this.factory
            .getAssociatedStylesheet(xml, null, null, null);
        assertThat(xsl, is(not(nullValue())));
        final Transformer transformer = this.factory.newTransformer(xsl);
        final StringWriter writer = new StringWriter();
        transformer.transform(xml, new StreamResult(writer));
        final String xhtml = writer.toString();
        assertThat(
            XmlConverters.the(xhtml),
            org.xmlmatchers.XmlMatchers.hasXPath("/html")
        );
    }

    private static final class InDirResolver implements URIResolver {
        private final File dir;
        public InDirResolver(final File path) {
            this.dir = path;
        }
        @Override
        public Source resolve(final String href, final String base) {
            try {
                final File xsl = new File(this.dir, href);
                FileUtils.writeStringToFile(
                    xsl,
                    new ContainerPage().page(href)
                );
                return new StreamSource(xsl);
            } catch (java.io.IOException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
    }

}
