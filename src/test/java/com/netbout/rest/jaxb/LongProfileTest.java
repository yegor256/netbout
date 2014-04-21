/**
 * Copyright (c) 2009-2014, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code accidentally and without intent to use it, please report this
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
package com.netbout.rest.jaxb;

import com.netbout.spi.IdentityMocker;
import com.rexsl.page.Link;
import com.rexsl.test.JaxbConverter;
import com.rexsl.test.XhtmlMatchers;
import java.io.File;
import javax.ws.rs.core.UriBuilder;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * Test case for {@link LongProfile}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class LongProfileTest {

    /**
     * LongProfile can be converted to XML.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void convertsToXml() throws Exception {
        final LongProfile obj = new LongProfile(
            UriBuilder.fromUri("http://localhost/foo?auth=foo"),
            new IdentityMocker().mock()
        );
        MatcherAssert.assertThat(
            JaxbConverter.the(obj),
            Matchers.allOf(
                XhtmlMatchers.hasXPath("//locales/link[@rel='locale']"),
                XhtmlMatchers.hasXPath("//link[code='en']"),
                XhtmlMatchers.hasXPath("//link[name='English']"),
                XhtmlMatchers.hasXPath("//link[language='English']"),
                XhtmlMatchers.hasXPath(
                    "//link[@href='http://localhost/foo/toggle?l=en&auth=foo']"
                )
            )
        );
    }

    /**
     * LongProfile can find only available locales.
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void returnsAvailableLocales() throws Exception {
        final LongProfile profile = new LongProfile(
            UriBuilder.fromUri("http://localhost"),
            new IdentityMocker().mock()
        );
        for (Link link : profile.getLocales()) {
            for (Object object : link.getElements()) {
                final Element element = (Element) object;
                if (!"code".equals(element.getTagName())) {
                    continue;
                }
                final File file = new File(
                    System.getProperty("basedir"),
                        String.format(
                        "/src/main/webapp/xml/lang/%s.xml",
                        element.getTextContent()
                    )
                );
                MatcherAssert.assertThat(
                    String.format("language file '%s' must be present", file),
                    file.exists()
                );
            }
        }
    }

}
