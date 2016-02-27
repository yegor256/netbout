/**
 * Copyright (c) 2009-2016, netbout.com
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

import com.google.common.base.Joiner;
import com.jcabi.matchers.XhtmlMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link MarkdownTxtmark}.
 *
 * @author Dmitry Zaytsev (dmitry.zaytsev@gmail.com)
 * @version $Id$
 * @since 2.23
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class MarkdownTxtmarkTest {
    /**
     * End of line.
     */
    private static final String EOL = "\n";

    /**
     * MarkdownTxtmark can handle whitespace after links.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void handlesWhitespaceAfterLinks() throws Exception {
        MatcherAssert.assertThat(
            new MarkdownTxtmark().html(
                "Hi [google](http://www.google.com) how are you?"
            ),
            Matchers.equalTo(
                // @checkstyle LineLengthCheck (1 line)
                "<p>Hi <a href=\"http://www.google.com\">google</a> how are you?</p>\n"
            )
        );
    }

    /**
     * MarkdownTxtmark can format a text to HTML.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void formatsTextToHtml() throws Exception {
        final String meta = new MarkdownTxtmark().html(
            Joiner.on(MarkdownTxtmarkTest.EOL).join(
                "**hi**, _dude_!\r",
                "",
                "     b**o",
                "       ",
                "        ",
                "    o**m",
                ""
            )
        );
        MatcherAssert.assertThat(
            String.format("<x>%s</x>", meta),
            Matchers.describedAs(
                meta,
                XhtmlMatchers.hasXPaths(
                    "/x/p/strong[.='hi']",
                    "/x/p/em[.='dude']",
                    Joiner.on(MarkdownTxtmarkTest.EOL).join(
                        "/x/pre/code[.=' b**o", "", "", "o**m", "']"
                    )
                )
            )
        );
    }

    /**
     * MarkdownTxtmark can handle broken formatting correctly.
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void handlesBrokenFormattingGracefully() throws Exception {
        final String[] texts = {
            Joiner.on(MarkdownTxtmarkTest.EOL).join("**", ""),
            "__",
            "",
            "**hi there! {{{",
            Joiner.on(MarkdownTxtmarkTest.EOL).join(
                "    ",
                " ",
                "      ",
                "     ",
                ""
            ),
        };
        for (final String text : texts) {
            MatcherAssert.assertThat(
                String.format("<z>%s</z>", new MarkdownTxtmark().html(text)),
                XhtmlMatchers.hasXPath("/z")
            );
        }
    }

    /**
     * MarkdownTxtmark will properly format reference link.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void handlesReferenceLinks() throws Exception {
        MatcherAssert.assertThat(
            new MarkdownTxtmark().html(
                // @checkstyle LineLengthCheck (1 line)
                "Reference-style: \n![alt text][logo]\n\n[logo]: https://camo.githubusercontent.com/f60dcff129bbc252ab48a4bace2aa92cc982774a/687474703a2f2f696d672e7465616d65642e696f2f62746e2e737667"
            ),
            Matchers.equalTo(
                // @checkstyle LineLengthCheck (1 line)
                "<p>Reference-style:<br  />\n<img src=\"https://camo.githubusercontent.com/f60dcff129bbc252ab48a4bace2aa92cc982774a/687474703a2f2f696d672e7465616d65642e696f2f62746e2e737667\" alt=\"alt text\" /><br  /></p>\n"
            )
        );
    }

    /**
     * MarkdownTxtmark will handle XSS violation.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void handlesScriptViolation() throws Exception {
        MatcherAssert.assertThat(
            new MarkdownTxtmark().html(
                "<script>alert()</script>"
            ),
            Matchers.equalTo(
                "<p>&lt;script>alert()&lt;/script></p>\n"
            )
        );
    }

    /**
     * MarkdownTxtmark can format small snippets.
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void formatsTextFragmentsToHtml() throws Exception {
        final String[][] texts = {
            new String[] {"hi, *dude*!", "<p>hi, <em>dude</em>!</p>"},
            new String[] {
                "hello, **dude**!",
                "<p>hello, <strong>dude</strong>!</p>",
            },
            new String[] {
                "wazzup, ***dude***!",
                "<p>wazzup, <strong><em>dude</em></strong>!</p>",
            },
            new String[] {
                "hey, _man_!",
                "<p>hey, <em>man</em>!</p>",
            },
            new String[] {
                "x: `oops`",
                "<p>x: <code>oops</code></p>",
            },
            new String[] {
                "[a](http://foo)",
                "<p><a href=\"http://foo\">a</a></p>",
            },
            new String[] {"}}}", "<p>}}}</p>"},
        };
        for (final String[] pair : texts) {
            MatcherAssert.assertThat(
                new MarkdownTxtmark().html(pair[0]).trim(),
                Matchers.equalTo(pair[1])
            );
        }
    }

    /**
     * MarkdownTxtmark can format bullets to HTML.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void formatsBulletsToHtml() throws Exception {
        final String meta = new MarkdownTxtmark().html(
            Joiner.on(MarkdownTxtmarkTest.EOL).join(
                "my list:",
                "",
                "* line one",
                "* line two",
                "",
                "normal text now"
            )
        );
        MatcherAssert.assertThat(
            String.format("<r>%s</r>", meta),
            Matchers.describedAs(
                meta,
                XhtmlMatchers.hasXPaths(
                    "/r/p[text()='my list:']",
                    "/r/ul[count(li) = 2]",
                    "/r/ul/li[text()='line one']",
                    "/r/ul/li[text()='line two']",
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
    public void breaksSingleLine() throws Exception {
        MatcherAssert.assertThat(
            new MarkdownTxtmark().html(
                Joiner.on(MarkdownTxtmarkTest.EOL)
                    .join("line1 line", "line2", "", "line3").trim()
            ),
            Matchers.equalTo(
                Joiner.on(MarkdownTxtmarkTest.EOL).join(
                    "<p>line1 line<br  />", "line2<br  /></p>",
                    "<p>line3</p>", ""
                )
            )
        );
    }

    /**
     * MarkdownTxtmark can leave DIV untouched.
     * @throws Exception If there is some problem inside
     */
    @Test
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
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void detectsLinks() throws Exception {
        final String[][] texts = {
            new String[] {
                "<a href=\"http://_google_.com\">g</a>",
                "<p><a href=\"http://_google_.com\">g</a></p>",
            },
            new String[] {
                "http://foo.com",
                "<p><a href=\"http://foo.com\">http://foo.com</a></p>",
            },
            new String[]{
                "(http://foo?com)",
                "<p>(<a href=\"http://foo?com\">http://foo?com</a>)</p>",
            },
            new String[] {
                "(http://foo#com)",
                "<p>(<a href=\"http://foo#com\">http://foo#com</a>)</p>",
            },
            new String[] {
                "(https://a?b=c)",
                "<p>(<a href=\"https://a?b=c\">https://a?b=c</a>)</p>",
            },
            new String[] {
                "[foo](http://foo)",
                "<p><a href=\"http://foo\">foo</a></p>",
            },
            new String[] {
                "[http://bar.com](http://bar.com)",
                "<p><a href=\"http://bar.com\">http://bar.com</a></p>",
            },
            new String[] {
                "[http://googl.com]",
                "<p>[<a href=\"http://googl.com\">http://googl.com</a>]</p>",
            },
            new String[] {
                "[google](http://www.google.com)",
                "<p><a href=\"http://www.google.com\">google</a></p>",
            },
            new String[] {
                Joiner.on(MarkdownTxtmarkTest.EOL).join(
                    "http://yahoo.com",
                    "http://bar.com [http://af.com](http://af.com) end"
                ),
                Joiner.on(MarkdownTxtmarkTest.EOL).join(
                    // @checkstyle LineLengthCheck (2 lines)
                    "<p><a href=\"http://yahoo.com\">http://yahoo.com</a><br  />",
                    "<a href=\"http://bar.com\">http://bar.com</a> <a href=\"http://af.com\">http://af.com</a> end</p>"
                ),
            },
            new String[] {
                "![logo]  (http://img.qulice.com/logo.svg)",
                // @checkstyle LineLengthCheck (1 line)
                "<p><img src=\"http://img.qulice.com/logo.svg\" alt=\"logo\" /></p>",
            },
            new String[] {
                "![logo](http://img.qulice.com/pict.svg)",
                // @checkstyle LineLengthCheck (1 line)
                "<p><img src=\"http://img.qulice.com/pict.svg\" alt=\"logo\" /></p>",
            },
        };
        for (final String[] pair : texts) {
            MatcherAssert.assertThat(
                new MarkdownTxtmark().html(pair[0]).trim(),
                Matchers.equalTo(pair[1])
            );
        }
    }

    /**
     * MarkdownTxtmark can escape the replacement string.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void escapesReplacement() throws Exception {
        MatcherAssert.assertThat(
            new MarkdownTxtmark().html("backslash \\ and group reference $3\n"),
            Matchers.is("<p>backslash \\ and group reference $3<br  /></p>\n")
        );
    }
}
