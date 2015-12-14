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
import org.apache.commons.lang3.StringUtils;
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
     * Start of html paragraph.
     */
    private static final String START_PARAGRAPH = "<p>";
    /**
     * End of html paragraph.
     */
    private static final String END_PARAGRAPH = "</p>";
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
                MarkdownTxtmarkTest.join(
                    "MarkdownTxtmarkTest.START_PARAGRAPHHi ",
                    // @checkstyle LineLengthCheck (1 line)
                    "<a href=\"http://www.google.com\">google</a> how are you?</p>",
                    ""
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
            MarkdownTxtmarkTest.join(
                "**hi**, _dude_!",
                "",
                "     b**o",
                "       ",
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
                    MarkdownTxtmarkTest.join(
                        "/x/pre/code[.=' b**o",
                        "",
                        "",
                        "o**m']"
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
    @Ignore
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void handlesBrokenFormattingGracefully() throws Exception {
        final String[] texts = {
            MarkdownTxtmarkTest.join("**", ""),
            "__",
            "",
            "**hi there! {{{",
            MarkdownTxtmarkTest.join("    ", " ", "      ", "     ", ""),
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
                MarkdownTxtmarkTest.join(
                    "<p>wazzup, ",
                    "<strong>",
                    "  <em>dude</em>",
                    "</strong>!</p>"
                ),
            },
            new String[] {
                "hey, _man_!",
                MarkdownTxtmarkTest.join("<p>hey, ", "<em>man</em>!</p>"),
            },
            new String[] {
                "x: `oops`",
                MarkdownTxtmarkTest.join("<p>x: ", "<code>oops</code></p>"),
            },
            new String[] {
                "[a](http://foo)",
                MarkdownTxtmarkTest.join(
                    MarkdownTxtmarkTest.START_PARAGRAPH,
                    "  <a href=\"http://foo\">a</a>",
                    MarkdownTxtmarkTest.END_PARAGRAPH
                ),
            },
            new String[] {"}}}\n", "<p>}}}</p>"},
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
    @Ignore
    public void formatsBulletsToHtml() throws Exception {
        final String meta = new MarkdownTxtmark().html(
            MarkdownTxtmarkTest.join(
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
            new MarkdownTxtmark().html(
                MarkdownTxtmarkTest.join("line1", "line2", "", "line3").trim()
            ),
            Matchers.equalTo(
                MarkdownTxtmarkTest.join(
                    "<p>line1",
                    "<br /",
                    ">line2</p>",
                    "<p>line3</p>"
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
                MarkdownTxtmarkTest.join(
                    MarkdownTxtmarkTest.START_PARAGRAPH,
                    "  <a href=\"http://_google_.com\">g</a>",
                    MarkdownTxtmarkTest.END_PARAGRAPH
                ),
            },
            new String[] {
                "http://foo.com",
                MarkdownTxtmarkTest.join(
                    MarkdownTxtmarkTest.START_PARAGRAPH,
                    "  ",
                    "<a href=\"http://foo.com\">http://foo.com</a>",
                    MarkdownTxtmarkTest.END_PARAGRAPH
                ),
            },
            new String[] {
                "(http://foo?com)",
                MarkdownTxtmarkTest.join(
                    MarkdownTxtmarkTest.START_PARAGRAPH,
                    "(",
                    "<a href=\"http://foo?com\">http://foo?com</a>)</p>"
                ),
            },
            new String[] {
                "(http://foo#com)",
                MarkdownTxtmarkTest.join(
                    "<p>(",
                    "<a href=\"http://foo#com\">http://foo#com</a>)</p>"
                ),
            },
            new String[] {
                "(https://a?b=c)",
                MarkdownTxtmarkTest.join(
                    "<p>(",
                    "<a href=\"https://a?b=c\">https://a?b=c</a>)</p>"
                ),
            },
            new String[] {
                "[foo](http://foo)",
                MarkdownTxtmarkTest.join(
                    MarkdownTxtmarkTest.START_PARAGRAPH,
                    "  <a href=\"http://foo\">foo</a>\n</p>"
                ),
            },
            new String[] {
                "[http://bar.com](http://bar.com)",
                MarkdownTxtmarkTest.join(
                    MarkdownTxtmarkTest.START_PARAGRAPH,
                    "  <a href=\"http://bar.com\">http://bar.com</a>",
                    MarkdownTxtmarkTest.END_PARAGRAPH
                ),
            },
            new String[] {
                "[http://googl.com]",
                "<p>[\n<a href=\"http://googl.com\">http://googl.com</a>]</p>",
            },
            new String[] {
                "[google](http://www.google.com)",
                MarkdownTxtmarkTest.join(
                    MarkdownTxtmarkTest.START_PARAGRAPH,
                    "  <a href=\"http://www.google.com\">google</a>",
                    MarkdownTxtmarkTest.END_PARAGRAPH
                ),
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
     * Join string separated by platform line separator.
     * @param elements Strings to join
     * @return Joined elements
     */
    private static String join(final String ... elements) {
        return StringUtils.join(elements, System.getProperty("line.separator"));
    }
}
