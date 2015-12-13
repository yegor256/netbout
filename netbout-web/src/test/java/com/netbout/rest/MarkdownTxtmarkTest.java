/**
 * Copyright (c) 2009-2015, netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netbout Inc. located at www.netbout.com.
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

import com.jcabi.matchers.XhtmlMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test case for {@link MarkdownTxtmark}.
 *
 * @author Dmitry Zaytsev (dmitry.zaytsev@gmail.com)
 * @version $Id$
 * @since 2.23
 * @todo #867:30min/DEV MarkdownTxtmark not implemented but has to be.
 *  See #847 for details.
 *  This class should be implementation of Markdown using TxtMark.
 *  See https://github.com/rjeschke/txtmark.
 *  After that don't forget remove Ignore annotation from unit tests.
 *
 */
public final class MarkdownTxtmarkTest {
    /**
     * MarkdownTxtmark can handle whitespace after links.
     * @throws Exception If there is some problem inside
     */
    @Test
    @Ignore
    public void handlesWhitespaceAfterLinks() throws Exception {
        MatcherAssert.assertThat(
            new MarkdownTxtmark().html(
                "Hi [google](http://www.google.com) how are you?"
            ),
            Matchers.equalTo(
                MarkdownTxtmarkTest.replaceNewLine(
                    // @checkstyle LineLengthCheck (1 line)
                    "<p>Hi \n<a href=\"http://www.google.com\">google</a> how are you?</p>\n"
                )
            )
        );
    }

    /**
     * MarkdownTxtmark can format a text to HTML.
     * @throws Exception If there is some problem inside
     */
    @Test
    @Ignore
    public void formatsTextToHtml() throws Exception {
        final String meta = new MarkdownTxtmark().html(
            "**hi**, _dude_!\r\n\n     b**o\n    \n    \n    o**m\n"
        );
        MatcherAssert.assertThat(
            String.format("<x>%s</x>", meta),
            Matchers.describedAs(
                meta,
                XhtmlMatchers.hasXPaths(
                    "/x/p/strong[.='hi']",
                    "/x/p/em[.='dude']",
                    "/x/pre/code[.=' b**o\n\n\no**m\n']"
                )
            )
        );
    }

    /**
     * MarkdownTxtmark can handle broken formatting correctly.
     * @throws Exception If there is some problem inside
     */
    @Test
    @Ignore
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void handlesBrokenFormattingGracefully() throws Exception {
        final String[] texts = {
            "**\n ",
            "__",
            "",
            "**hi there! {{{",
            "    \n  \n      \n     \n",
        };
        for (final String text : texts) {
            MatcherAssert.assertThat(
                String.format("<z>%s</z>", new MarkdownTxtmark().html(text)),
                XhtmlMatchers.hasXPath("/z")
            );
        }
    }

    /**
     * MarkdownTxtmark can format small snippets.
     * @throws Exception If there is some problem inside
     */
    @Test
    @Ignore
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void formatsTextFragmentsToHtml() throws Exception {
        final String[][] texts = {
            new String[] {"hi, *dude*!", "<p>hi, \n<em>dude</em>!</p>"},
            new String[] {
                "hello, **dude**!",
                "<p>hello, \n<strong>dude</strong>!</p>",
            },
            new String[] {
                "wazzup, ***dude***!",
                "<p>wazzup, \n<strong>\n  <em>dude</em>\n</strong>!</p>",
            },
            new String[] {"hey, _man_!", "<p>hey, \n<em>man</em>!</p>"},
            new String[] {"x: `oops`", "<p>x: \n<code>oops</code></p>"},
            new String[] {
                "[a](http://foo)",
                "<p>\n  <a href=\"http://foo\">a</a>\n</p>",
            },
            new String[] {"}}}\n", "<p>}}}</p>"},
        };
        for (final String[] pair : texts) {
            MatcherAssert.assertThat(
                new MarkdownTxtmark().html(pair[0]).trim(),
                Matchers.equalTo(
                    // @checkstyle MultipleStringLiteralsCheck (1 line)
                    pair[1].replace("\n", System.getProperty("line.separator"))
                )
            );
        }
    }

    /**
     * MarkdownTxtmark can format bullets to HTML.
     * @throws Exception If there is some problem inside
     */
    @Test
    @Ignore
    public void formatsBulletsToHtml() throws Exception {
        final String meta = new MarkdownTxtmark().html(
            "my list:\n\n* line one\n* line two\n\nnormal text now"
        );
        MatcherAssert.assertThat(
            String.format("<r>%s</r>", meta),
            Matchers.describedAs(
                meta,
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

    /**
     * MarkdownTxtmark can break a single line.
     * @throws Exception If there is some problem inside
     */
    @Test
    @Ignore
    public void breaksSingleLine() throws Exception {
        MatcherAssert.assertThat(
            new MarkdownTxtmark().html("line1\nline2\n\nline3").trim(),
            Matchers.equalTo(
                MarkdownTxtmarkTest.replaceNewLine(
                    "<p>line1\n<br />\nline2</p>\n<p>line3</p>"
                )
            )
        );
    }

    /**
     * MarkdownTxtmark can leave DIV untouched.
     * @throws Exception If there is some problem inside
     */
    @Test
    @Ignore
    public void leavesDivUntouched() throws Exception {
        MatcherAssert.assertThat(
            new MarkdownTxtmark().html("<div>hey<svg viewBox='444'/></div>"),
            XhtmlMatchers.hasXPaths(
                "/div/svg[@viewBox]"
            )
        );
    }

    /**
     * MarkdownTxtmark can detect plain text links and produce HTML
     * with links wrapped correctly.
     * @throws Exception If there is some problem inside
     */
    @Test
    @Ignore
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void detectsLinks() throws Exception {
        final String[][] texts = {
            new String[] {
                "<a href=\"http://_google_.com\">g</a>",
                "<p>\n  <a href=\"http://_google_.com\">g</a>\n</p>",
            },
            new String[] {
                "http://foo.com",
                "<p>\n  <a href=\"http://foo.com\">http://foo.com</a>\n</p>",
            },
            new String[] {
                "(http://foo?com)",
                "<p>(\n<a href=\"http://foo?com\">http://foo?com</a>)</p>",
            },
            new String[] {
                "(http://foo#com)",
                "<p>(\n<a href=\"http://foo#com\">http://foo#com</a>)</p>",
            },
            new String[] {
                "(https://a?b=c)",
                "<p>(\n<a href=\"https://a?b=c\">https://a?b=c</a>)</p>",
            },
            new String[] {
                "[foo](http://foo)",
                "<p>\n  <a href=\"http://foo\">foo</a>\n</p>",
            },
            new String[] {
                "[http://bar.com](http://bar.com)",
                "<p>\n  <a href=\"http://bar.com\">http://bar.com</a>\n</p>",
            },
            new String[] {
                "[http://googl.com]",
                "<p>[\n<a href=\"http://googl.com\">http://googl.com</a>]</p>",
            },
            new String[] {
                "[google](http://www.google.com)",
                "<p>\n  <a href=\"http://www.google.com\">google</a>\n</p>",
            },
        };
        for (final String[] pair : texts) {
            MatcherAssert.assertThat(
                new MarkdownTxtmark().html(pair[0]).trim(),
                Matchers.equalTo(
                    MarkdownTxtmarkTest.replaceNewLine(pair[1])
                )
            );
        }
    }

    /**
     * Replace '\n' to the platform line separator.
     * @param source Source string
     * @return Changed string
     */
    private static String replaceNewLine(final String source) {
        return   source.replace("\n", System.getProperty("line.separator"));
    }
}
