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
package com.netbout.hub.predicates.xml;

import com.netbout.hub.Hub;
import com.netbout.hub.HubMocker;
import com.rexsl.test.ContainerMocker;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case of {@link DomText}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class DomTextTest {

    /**
     * URL of XSD schema.
     */
    private transient String xsd;

    /**
     * Valid XML document.
     */
    private transient String xml;

    /**
     * Prepare a provider of XSD schema.
     * @throws Exception If there is some problem inside
     */
    @Before
    public void prepareXsd() throws Exception {
        this.xsd = new ContainerMocker()
            .expectMethod(Matchers.equalTo("GET"))
            .returnBody(
                "<schema xmlns='http://www.w3.org/2001/XMLSchema'"
                + " xmlns:foo='foo' targetNamespace='foo'>"
                + "<element name='root'/></schema>"
            )
            .returnHeader("Content-Type", "application/xml")
            .mock()
            .home()
            .toURL()
            .toString();
        this.xml = "<root xmlns='foo'"
            + " xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'"
            + String.format(" xsi:schemaLocation='foo %s'", this.xsd)
            + "/>";
    }

    /**
     * DomText can detect an XML document.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void positivelyMatchesXmlDocument() throws Exception {
        MatcherAssert.assertThat("it's an XML", new DomText("<root/>").isXml());
        MatcherAssert.assertThat("it's not", !new DomText("test").isXml());
    }

    /**
     * DomText can detect a namespace of XML document.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void detectsNamespaceOfXmlDocument() throws Exception {
        MatcherAssert.assertThat(
            new DomText(this.xml).namespace(),
            Matchers.equalTo("foo")
        );
    }

    /**
     * DomText can validate a document through helpers.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void validatesCorrectDocument() throws Exception {
        final Hub hub = new HubMocker().mock();
        new DomText(this.xml).validate(hub);
    }

    /**
     * DomText throws exception for invalid document.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = DomValidationException.class)
    public void validatesIncorrectDocument() throws Exception {
        final Hub hub = new HubMocker().mock();
        new DomText("<some-document/>").validate(hub);
    }

}
