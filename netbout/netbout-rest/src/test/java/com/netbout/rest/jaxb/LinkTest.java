/**
 * Copyright (c) 2009-2012, Netbout.com
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
package com.netbout.rest.jaxb;

import com.netbout.rest.page.JaxbBundle;
import com.rexsl.test.JaxbConverter;
import com.rexsl.test.XhtmlMatchers;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Link}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class LinkTest {

    /**
     * Link can be converted to XML.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void convertsToXml() throws Exception {
        final Link link = new Link(
            "foo",
            UriBuilder.fromUri("http://bar").build(),
            MediaType.TEXT_XML
        );
        link.add(new JaxbBundle("label", "some label").element());
        MatcherAssert.assertThat(
            JaxbConverter.the(link),
            Matchers.allOf(
                XhtmlMatchers.hasXPath("/link[@rel='foo']"),
                XhtmlMatchers.hasXPath("/link[@href='http://bar']"),
                XhtmlMatchers.hasXPath("/link[@type='text/xml']"),
                XhtmlMatchers.hasXPath("/link[label='some label']")
            )
        );
    }

    /**
     * Link can hide an empty label.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void hidesEmptyLabel() throws Exception {
        final Link obj = new Link(
            "some rel name",
            UriBuilder.fromUri("http://foo").build()
        );
        MatcherAssert.assertThat(
            JaxbConverter.the(obj),
            XhtmlMatchers.hasXPath("/link[not(@label)]")
        );
    }

}
