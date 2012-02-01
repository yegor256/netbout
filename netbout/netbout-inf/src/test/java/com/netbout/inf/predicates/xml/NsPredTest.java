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
package com.netbout.inf.predicates.xml;

import com.netbout.inf.MsgMocker;
import com.netbout.inf.Predicate;
import com.netbout.inf.PredicateMocker;
import com.rexsl.test.ContainerMocker;
import java.util.Arrays;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case of {@link NsPred}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class NsPredTest {

    /**
     * URL of XSD schema.
     */
    private transient String xsd;

    /**
     * Prepare a provider of XSD schema.
     * @throws Exception If there is some problem inside
     */
    @Before
    public void prepareXsd() throws Exception {
        this.xsd = new ContainerMocker()
            .expectMethod(Matchers.equalTo("GET"))
            .returnBody(
                // @checkstyle StringLiteralsConcatenation (3 lines)
                "<schema xmlns='http://www.w3.org/2001/XMLSchema'"
                + " xmlns:foo='urn:test:foo' targetNamespace='urn:test:foo'>"
                + "<element name='root'/></schema>"
            )
            .returnHeader("Content-Type", "application/xml")
            .mock()
            .home()
            .toURL()
            .toString();
    }

    /**
     * NsPred can match an XML document.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void positivelyMatchesXmlDocument() throws Exception {
        final Predicate pred = new NsPred(
            Arrays.asList(
                new Predicate[] {
                    new PredicateMocker().doReturn("urn:test:foo").mock(),
                }
            )
        );
        MatcherAssert.assertThat(
            "matched",
            (Boolean) pred.evaluate(
                new MsgMocker().with(
                    "text",
                    // @checkstyle StringLiteralsConcatenation (7 lines)
                    "<root xmlns='urn:test:foo'"
                    + " xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'"
                    + String.format(
                        " xsi:schemaLocation='urn:test:foo %s'",
                        this.xsd
                    )
                    + "/>"
                ).mock(),
                0
            )
        );
    }

    /**
     * NsPred can match an XML document.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void negativelyMatchesNonXmlDocument() throws Exception {
        final Predicate pred = new NsPred(
            Arrays.asList(
                new Predicate[] {
                    new PredicateMocker().doReturn("urn:test:different").mock(),
                }
            )
        );
        MatcherAssert.assertThat(
            "not matched",
            !(Boolean) pred.evaluate(
                new MsgMocker().with("text", "some non-XML text").mock(),
                0
            )
        );
    }

}
