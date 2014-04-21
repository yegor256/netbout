/**
 * Copyright (c) 2009-2014, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code accidentally and without intent to use it, please report this
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

import com.rexsl.test.XhtmlMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Markdown}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class MarkdownTest {

    /**
     * Markdown can format a text to HTML.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void formatsTextToHtml() throws Exception {
        final Markdown meta = new Markdown(
            "**hi**, _dude_!\r\n\n     b**o\n    \n    \n    o**m\n"
        );
        MatcherAssert.assertThat(
            String.format("<x>%s</x>", meta.html()),
            Matchers.describedAs(
                meta.html(),
                XhtmlMatchers.hasXPaths(
                    "/x/p/strong[.='hi']",
                    "/x/p/em[.='dude']",
                    "/x/pre/code[.=' b**o\n\n\no**m\n']"
                )
            )
        );
    }

    /**
     * Markdown can format a meta-text to plain text.
     * @throws Exception If there is some problem inside
     * @todo #481 Waiting for https://github.com/lruiz/MarkdownPapers/issues/27
     */
    @Test
    @org.junit.Ignore
    public void formatsMarkdownToPlain() throws Exception {
        final Markdown meta = new Markdown(
            "**hi**, _buddy_!\r\n\n     b**o\n    \n    \n    o**m\n"
        );
        MatcherAssert.assertThat(
            meta.plain(),
            Matchers.equalTo("hi, buddy!\n\n b**o\n\n\no**m")
        );
    }

    /**
     * Markdown can handle broken formatting correctly.
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
                String.format("<z>%s</z>", new Markdown(text).html()),
                XhtmlMatchers.hasXPath("/z")
            );
        }
    }

    /**
     * Markdown can format small snippets.
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void formatsTextFragmentsToHtml() throws Exception {
        final String[][] texts = new String[][] {
            new String[] {"hi, *dude*!", "<p>hi, <em>dude</em>!</p>"},
            new String[] {
                "hello, **dude**!",
                "<p>hello, <strong>dude</strong>!</p>",
            },
            new String[] {
                "wazzup, ***dude***!",
                "<p>wazzup, <strong><em>dude</em></strong>!</p>",
            },
            new String[] {"hey, _man_!", "<p>hey, <em>man</em>!</p>"},
            new String[] {"x: `oops`", "<p>x: <code>oops</code></p>"},
            new String[] {
                "[a](http://foo)",
                "<p><a href=\"http://foo\">a</a></p>",
            },
            new String[] {"}}}\n", "<p>}}}</p>"},
        };
        for (String[] pair : texts) {
            MatcherAssert.assertThat(
                new Markdown(pair[0]).html().trim(),
                Matchers.equalTo(pair[1])
            );
        }
    }

    /**
     * Markdown can format bullets to HTML.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void formatsBulletsToHtml() throws Exception {
        final Markdown meta = new Markdown(
            "my list:\n\n* line one\n* line two\n\nnormal text now"
        );
        MatcherAssert.assertThat(
            String.format("<r>%s</r>", meta.html()),
            Matchers.describedAs(
                meta.html(),
                XhtmlMatchers.hasXPaths(
                    "/r/p[.='my list:']",
                    "/r/ul[count(li) = 2]",
                    "/r/ul/li[.='line one']",
                    "/r/ul/li[.='line two']",
                    "/r/p[.='normal text now']"
                )
            )
        );
    }

}
