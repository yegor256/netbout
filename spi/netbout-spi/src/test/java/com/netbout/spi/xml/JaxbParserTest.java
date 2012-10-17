/**
 * Copyright (c) 2009-2012, Netbout.com
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

import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link JaxbParser}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class JaxbParserTest {

    /**
     * JaxbParser can parse XML and return an object.
     * @throws Exception If some problem inside
     */
    @Test
    public void parsesTextAndReturnsObject() throws Exception {
        final Long number = Math.abs(new Random().nextLong());
        final JaxbParser parser = new JaxbParser(
            String.format(
                "<token xmlns='%s'><number>%d</number></token>",
                TokenMocker.NAMESPACE,
                number
            )
        );
        MatcherAssert.assertThat(
            parser.parse(TokenMocker.class),
            Matchers.allOf(
                Matchers.notNullValue(),
                Matchers.instanceOf(TokenMocker.class),
                Matchers.hasProperty("number", Matchers.equalTo(number))
            )
        );
    }

    /**
     * JaxbParser can parse XML and return an object, without schemas.
     * @throws Exception If some problem inside
     */
    @Test
    public void parsesTextAndReturnsObjectWithoutSchema() throws Exception {
        final String name = "Peter";
        final JaxbParser parser = new JaxbParser(
            String.format("<foo><name>%s</name></foo>", name)
        );
        MatcherAssert.assertThat(
            parser.parse(FooMocker.class),
            Matchers.allOf(
                Matchers.notNullValue(),
                Matchers.instanceOf(FooMocker.class),
                Matchers.hasProperty("name", Matchers.equalTo(name))
            )
        );
    }

    /**
     * Jaxb can detect namespace.
     * @throws Exception If some problem inside
     */
    @Test
    public void detectsNamespace() throws Exception {
        MatcherAssert.assertThat(
            "NULL input is processed gracefully",
            !new JaxbParser(null).has(TokenMocker.class)
        );
        MatcherAssert.assertThat(
            "non-XML document is processed gracefully",
            !new JaxbParser("abc").has(TokenMocker.class)
        );
        MatcherAssert.assertThat(
            "no namespaces in this document",
            !new JaxbParser("<x/>").has(TokenMocker.class)
        );
        MatcherAssert.assertThat(
            "namespace is in place",
            new JaxbParser(
                String.format("<root xmlns='%s'/>", TokenMocker.NAMESPACE)
            ).has(TokenMocker.class)
        );
    }

}
