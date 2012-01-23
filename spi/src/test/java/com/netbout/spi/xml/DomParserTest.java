/**
 * Copyright (c) 2009-2011, NetBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi.xml;

import com.netbout.spi.Urn;
import com.rexsl.test.ContainerMocker;
import com.rexsl.test.XhtmlConverter;
import com.rexsl.test.XhtmlMatchers;
import java.net.URL;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link DomParser}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class DomParserTest {

    /**
     * DomParser can parse XML and return a DOM tree.
     * @throws Exception If some problem inside
     */
    @Test
    public void parsesTextAndReturnsDomTree() throws Exception {
        final DomParser parser = new DomParser(
            "<token><number>15</number></token>"
        );
        MatcherAssert.assertThat(
            XhtmlConverter.the(new DomPrinter(parser.parse()).print()),
            XhtmlMatchers.hasXPath("/token/number[.=15]")
        );
    }

    /**
     * DomParser can detect namespaces.
     * @throws Exception If some problem inside
     */
    @Test
    public void detectsNamespace() throws Exception {
        final DomParser parser = new DomParser(
            "<token xmlns='foo'><number>15</number></token>"
        );
        MatcherAssert.assertThat(
            parser.parse().getDocumentElement().getNamespaceURI(),
            Matchers.equalTo("foo")
        );
    }

    /**
     * DomParser can detect namespace in {@code belongsTo()} call.
     * @throws Exception If some problem inside
     */
    @Test
    public void detectsNamespaceWIthBelongsToMethod() throws Exception {
        MatcherAssert.assertThat(
            "this XML document belongs to the namespace mentioned",
            new DomParser("<foo:x xmlns:foo='urn:test:foo'/>").belongsTo(
                new Urn("urn:test:foo")
            )
        );
    }

    /**
     * DomParser can validate a document against its schema.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void validatesCorrectDocument() throws Exception {
        final String schema =
            // @checkstyle StringLiteralsConcatenation (10 lines)
            "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'"
            + " xmlns:p='foo' targetNamespace='foo'"
            + " elementFormDefault='qualified'>"
            + "<xs:element name='root' type='p:main'/>"
            + "<xs:complexType name='main'>"
            + "<xs:sequence>"
            + "<xs:element name='alpha' type='xs:string' />"
            + "</xs:sequence>"
            + "</xs:complexType>"
            + "</xs:schema>";
        final URL xsd = new URL(
            new ContainerMocker()
                .expectMethod(Matchers.equalTo("GET"))
                .returnBody(schema)
                .returnHeader("Content-Type", "application/xml")
                .mock()
                .home()
                .toURL()
                .toString()
        );
        // @checkstyle StringLiteralsConcatenation (4 lines)
        final String xml = "<root xmlns='foo'"
            + " xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'"
            + String.format(" xsi:schemaLocation='foo %s'", xsd)
            + "><alpha>xxx</alpha></root>";
        new DomParser(xml).validate();
    }

    /**
     * DomParser throws exception for invalid document.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = DomValidationException.class)
    public void validatesIncorrectDocument() throws Exception {
        new DomParser("<some-document/>").validate();
    }

}
