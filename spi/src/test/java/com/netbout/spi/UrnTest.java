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
package com.netbout.spi;

import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Urn}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class UrnTest {

    /**
     * Urn can be instantiated from plain text.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void instantiatesFromText() throws Exception {
        final Urn urn = new Urn("urn:netbout:jeff-lebowski");
        MatcherAssert.assertThat(urn.nid(), Matchers.equalTo("netbout"));
        MatcherAssert.assertThat(urn.nss(), Matchers.equalTo("jeff-lebowski"));
    }

    /**
     * Urn can throw exception when text is NULL.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenTextIsNull() throws Exception {
        new Urn(null);
    }

    /**
     * Urn can be instantiated from components.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void instantiatesFromComponents() throws Exception {
        final String nid = "foo";
        final String nss = "\u8416 & \u8415 *&^%$#@!-~`\"'";
        final Urn urn = new Urn(nid, nss);
        MatcherAssert.assertThat(urn.nid(), Matchers.equalTo(nid));
        MatcherAssert.assertThat(urn.nss(), Matchers.equalTo(nss));
        MatcherAssert.assertThat(urn.toURI(), Matchers.instanceOf(URI.class));
    }

    /**
     * Urn can throw exception when NID is NULL.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenNidIsNull() throws Exception {
        new Urn(null, "some-test-nss");
    }

    /**
     * Urn can throw exception when NSS is NULL.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenNssIsNull() throws Exception {
        new Urn("namespace1", null);
    }

    /**
     * Urn can be tested for equivalence of another Urn.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void comparesForEquivalence() throws Exception {
        final String text = "urn:foo:some-other-specific-string";
        final Urn first = new Urn(text);
        final Urn second = new Urn(text);
        MatcherAssert.assertThat(first, Matchers.equalTo(second));
    }

    /**
     * Urn can be tested for equivalence with another URI.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void comparesForEquivalenceWithUri() throws Exception {
        final String text = "urn:foo:some-specific-string";
        final Urn first = new Urn(text);
        final URI second = new URI(text);
        MatcherAssert.assertThat(first.equals(second), Matchers.is(true));
    }

    /**
     * Urn can be tested for equivalence with string.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void comparesForEquivalenceWithString() throws Exception {
        final String text = "urn:foo:some-text-as-text";
        final Urn first = new Urn(text);
        MatcherAssert.assertThat(first.equals(text), Matchers.is(true));
    }

    /**
     * Urn can be converted to string.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void convertsToString() throws Exception {
        final String text = "urn:foo:text-of-urn";
        final Urn urn = new Urn(text);
        MatcherAssert.assertThat(urn.toString(), Matchers.equalTo(text));
    }

    /**
     * Urn can catch incorrect syntax.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = java.net.URISyntaxException.class)
    public void catchesIncorrectUrnSyntax() throws Exception {
        new Urn("some incorrect name");
    }

    /**
     * Urn can pass correct syntax.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void passesCorrectUrnSyntax() throws Exception {
        final String[] texts = new String[] {
            "urn:foo:Some+Text+With+Spaces",
            "urn:foo:some%20text%20with%20spaces",
            "urn:a:",
            "urn:a:?alpha=50",
            "urn:a:?alpha=50&beta=u%20-works-fine",
            "urn:verylongnamespaceid:",
        };
        for (String text : texts) {
            final Urn urn = Urn.create(text);
            MatcherAssert.assertThat(
                Urn.create(urn.toString()),
                Matchers.equalTo(urn)
            );
            MatcherAssert.assertThat("is valid", Urn.isValid(urn.toString()));
        }
    }

    /**
     * Urn can throw exception for incorrect syntax.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void throwsExceptionForIncorrectUrnSyntax() throws Exception {
        final String[] texts = new String[] {
            "abc",
            "",
            "urn::",
            "urn:incorrect namespace name with spaces:test",
            "urn:abc+foo:test-me",
            "urn:test:?abc?",
            "urn:incorrect%20namespace:",
            "urn:verylongnameofanamespaceverylongnameofanamespace:",
            "urn:test:spaces are not allowed here",
            "urn:test:unicode-has-to-be-encoded:\u8514",
        };
        for (String text : texts) {
            try {
                Urn.create(text);
                MatcherAssert.assertThat(text, Matchers.nullValue());
            } catch (IllegalArgumentException ex) {
                assert ex != null;
            }
        }
    }

    /**
     * Urn can be "empty".
     * @throws Exception If there is some problem inside
     */
    @Test
    public void emptyUrnIsAFirstClassCitizen() throws Exception {
        final Urn urn = new Urn();
        MatcherAssert.assertThat(urn.isEmpty(), Matchers.equalTo(true));
    }

    /**
     * Urn can be "empty" only in one form.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = IllegalArgumentException.class)
    public void emptyUrnHasOnlyOneVariant() throws Exception {
        new Urn("void", "it-is-impossible-to-have-any-NSS-here");
    }

    /**
     * Urn can be "empty" only in one form, with from-text ctor.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = java.net.URISyntaxException.class)
    public void emptyUrnHasOnlyOneVariantWithTextCtor() throws Exception {
        new Urn("urn:void:it-is-impossible-to-have-any-NSS-here");
    }

    /**
     * Urn can match a pattern.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void matchesPatternWithAnotherUrn() throws Exception {
        MatcherAssert.assertThat(
            "matches",
            new Urn("urn:test:file").matches(new Urn("urn:test:*"))
        );
    }

    /**
     * Urn can retrieve params.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void retrievesParamsByName() throws Exception {
        MatcherAssert.assertThat(
            new Urn("urn:test:x?a=some-value").param("a"),
            Matchers.equalTo("some-value")
        );
    }

}
