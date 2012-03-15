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
package com.netbout.rest;

import java.net.HttpCookie;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test case for {@link CookieBuilder}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class CookieBuilderTest {

    /**
     * CookieBuilder can accept correct values.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void acceptsValidValues() throws Exception {
        final String[] texts = new String[] {
            "",
            "text",
            "some-text-to-accept(!)",
        };
        for (String text : texts) {
            new CookieBuilder(new URI("http://localhost/foo"))
                .named("some-name")
                .valued(text)
                .build();
        }
    }

    /**
     * CookieBuilder can reject incorrect values.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void rejectsInvalidValues() throws Exception {
        final String[] texts = new String[] {
            " ",
            ";",
            "\\ backslash is not allowed",
        };
        for (String text : texts) {
            try {
                new CookieBuilder(new URI("http://localhost/foo"))
                    .named("some-name")
                    .valued(text)
                    .build();
                Assert.fail("Exception expected here");
            } catch (IllegalArgumentException ex) {
                assert ex != null;
            }
        }
    }

    /**
     * CookieBuilder can build a valid cookie.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void buildsCorrectCookie() throws Exception {
        final String name = "some-cookie-name";
        final String value = "some-value-of-it";
        final String cookie = new CookieBuilder(new URI("http://localhost/a"))
            .named(name)
            .valued(value)
            .build()
            .toString();
        MatcherAssert.assertThat(
            HttpCookie.parse(cookie).get(0),
            Matchers.allOf(
                Matchers.hasToString(Matchers.containsString(name)),
                Matchers.hasProperty("name", Matchers.equalTo(name)),
                Matchers.hasProperty("value", Matchers.equalTo(value)),
                Matchers.hasProperty("domain", Matchers.equalTo("localhost")),
                Matchers.hasProperty("path", Matchers.equalTo("/a"))
            )
        );
    }

}
