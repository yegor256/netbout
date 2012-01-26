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
package com.netbout.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link TextUtils}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class TextUtilsTest {

    /**
     * TextUtils can convert text to Base64 and backward.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void convertsBaseToTextAndBack() throws Exception {
        final String[] texts = new String[] {
            "\u041F\u0435\u0442\u0440 I",
            "",
            "a",
            "abc",
            "\u041F\n\r\t    ",
            // @checkstyle MagicNumber (1 line)
            StringUtils.repeat("ABC ", 1000),
        };
        for (String text : texts) {
            final String encoded = TextUtils.pack(text);
            MatcherAssert.assertThat(
                "Encoded string contains only valid characters",
                encoded.matches("[\\w=\\+\\./]*"),
                Matchers.describedAs(encoded, Matchers.is(true))
            );
            MatcherAssert.assertThat(
                "Encoded string doesn't contain any special chars",
                encoded.contains("\n"),
                Matchers.is(false)
            );
            MatcherAssert.assertThat(
                "Decoded version matches the original one",
                TextUtils.unpack(encoded),
                Matchers.equalTo(text)
            );
        }
    }

    /**
     * TextUtils can format Velocity template.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void formatsVelocityTemplate() throws Exception {
        final VelocityContext context = new VelocityContext();
        final String xsl = TextUtils.format(
            "com/netbout/rest/wrapper.xsl.vm",
            context
        );
        MatcherAssert.assertThat(
            "Output XSL stylesheet is valid",
            xsl,
            Matchers.containsString("xsl:include")
        );
    }

}
