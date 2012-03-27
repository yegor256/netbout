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
package com.netbout.rest.meta;

import com.rexsl.test.XhtmlConverter;
import com.rexsl.test.XhtmlMatchers;
import java.util.Map;
import org.apache.commons.lang.ArrayUtils;
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
            "**hi**, _dude_!\r\n     b**o\n    \n    \n    o**m\n"
        );
        MatcherAssert.assertThat(
            XhtmlConverter.the(String.format("<x>%s</x>", meta.html())),
            Matchers.describedAs(
                meta.html(),
                Matchers.allOf(
                    XhtmlMatchers.hasXPath("/x/p/b[.='hi']"),
                    XhtmlMatchers.hasXPath("/x/p/i[.='dude']"),
                    XhtmlMatchers.hasXPath(
                        "/x/p[@class='fixed' and .=' b**o\n\n\no**m']"
                    )
                )
            )
        );
    }

    /**
     * MetaText can format a meta-text to plain text.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void formatsMetaTextToPlain() throws Exception {
        final MetaText meta = new MetaText(
            "**hi**, _buddy_!\r\n     b**o\n    \n    \n    o**m\n"
        );
        MatcherAssert.assertThat(
            meta.plain(),
            Matchers.equalTo("hi, buddy!\n\n b**o\n\n\no**m")
        );
    }

    /**
     * MetaText can handle broken formatting correctly.
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void handlesBrokenFormattingGracefully() throws Exception {
        final String[] texts = new String[] {
            "**\n ",
            "__",
            "",
            "**hi there! {{{",
            "    \n  \n      \n     \n",
        };
        for (String text : texts) {
            MatcherAssert.assertThat(
                XhtmlConverter.the(
                    String.format("<z>%s</z>", new MetaText(text).html())
                ),
                XhtmlMatchers.hasXPath("/z")
            );
        }
    }

    /**
     * MetaText can format small snippets.
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings({
        "PMD.AvoidInstantiatingObjectsInLoops", "PMD.UseConcurrentHashMap"
    })
    public void formatsTextFragmentsToHtml() throws Exception {
        final Map<String, String> texts = ArrayUtils.toMap(
            new Object[][] {
                {"hi, *dude*!", "<p>hi, <b>dude</b>!</p>"},
                {"hello, **dude**!", "<p>hello, <b>dude</b>!</p>"},
                {"wazzup, ***dude***!", "<p>wazzup, <b>dude</b>!</p>"},
                {"hey, _man_!", "<p>hey, <i>man</i>!</p>"},
                {"x: `oops`", "<p>x: <span class='tt'>oops</span></p>"},
                {"[a](http://foo)", "<p><a href='http://foo'>a</a></p>"},
                {"}}}\n", "<p>}}}</p>"},
            }
        );
        for (Map.Entry<String, String> entry : texts.entrySet()) {
            MatcherAssert.assertThat(
                new MetaText(entry.getKey()).html(),
                Matchers.equalTo(entry.getValue())
            );
        }
    }

}
