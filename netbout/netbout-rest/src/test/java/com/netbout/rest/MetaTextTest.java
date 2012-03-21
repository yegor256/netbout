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

import com.rexsl.test.XhtmlConverter;
import com.rexsl.test.XhtmlMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link MetaText}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class MetaTextTest {

    /**
     * MetaText can format a text to HTML.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void formatsTextToHtml() throws Exception {
        final MetaText meta = new MetaText(
            "**hi**, _dude_!\r\n{{{\r\nb**oo**m\n}}}"
        );
        MatcherAssert.assertThat(
            XhtmlConverter.the(String.format("<x>%s</x>", meta.html())),
            Matchers.describedAs(
                meta.html(),
                Matchers.allOf(
                    XhtmlMatchers.hasXPath("/x/b[.='hi']"),
                    XhtmlMatchers.hasXPath("/x/i[.='dude']"),
                    XhtmlMatchers.hasXPath(
                        "/x/div[@class='fixed' and .='b**oo**m']"
                    )
                )
            )
        );
    }

    /**
     * MetaText can handle broken formatting correctly.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void handlesBrokenFormattingGracefully() throws Exception {
        final String[] texts = new String[] {
            "}}}\n",
            "{{{",
            "",
            "**hi there! {{{",
            "{{{\n{{{\n{{{\n}}}\n",
            "}}}\n}}}\n{{{\n}}}\n}}}\n",
        };
        for (String text : texts) {
            MatcherAssert.assertThat(
                XhtmlConverter.the(
                    String.format("<x>%s</x>", new MetaText(text).html())
                ),
                XhtmlMatchers.hasXPath("/x")
            );
        }
    }

}
