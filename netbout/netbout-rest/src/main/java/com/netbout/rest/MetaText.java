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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Text with meta commands.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class MetaText {

    /**
     * The source text.
     */
    private final transient String text;

    /**
     * Public ctor.
     * @param txt The raw source text, with meta commands
     */
    public MetaText(final String txt) {
        this.text = txt;
    }

    /**
     * Convert it to HTML.
     * @return The HTML
     */
    public String html() {
        return StringUtils.join(
            this.paragraphs(
                new AbstractPar(
                    ArrayUtils.toMap(
                        new Object[][] {
                            // @checkstyle MultipleStringLiterals (5 lines)
                            // @checkstyle LineLength (1 line)
                            {"\\[(.*?)\\]\\((http://.*?)\\)", "<a href='$2'>$1</a>"},
                            {"\\*+(.*?)\\*+", "<b>$1</b>"},
                            {"`(.*?)`", "<span class='tt'>$1</span>"},
                            {"_+(.*?)_+", "<i>$1</i>"},
                        }
                    )
                ) {
                    @Override
                    protected String pack() {
                        String out;
                        if (this.isPre()) {
                            out = String.format(
                                "<p class='fixed'>%s</p>",
                                this.getText()
                            );
                        } else {
                            out = String.format("<p>%s</p>", this.getText());
                        }
                        return out;
                    }
                }
            ),
            ""
        );
    }

    /**
     * Convert it to plain text.
     * @return The plain text
     */
    public String plain() {
        return StringUtils.join(
            this.paragraphs(
                new AbstractPar(
                    ArrayUtils.toMap(
                        new Object[][] {
                            // @checkstyle MultipleStringLiterals (4 lines)
                            {"\\[(.*?)\\]\\((http://.*?)\\)", "$1 ($2)"},
                            {"\\*\\*(.*?)\\*\\*", "$1"},
                            {"`(.*?)`", "$1"},
                            {"_(.*?)_", "$1"},
                        }
                    )
                ) {
                    @Override
                    protected String pack() {
                        return this.getText();
                    }
                }
            ),
            "\n\n"
        );
    }

    /**
     * Break text down do paragraphs.
     * @param par Paragraph processor
     * @return List of paragraphs found
     */
    private List<String> paragraphs(final Par par) {
        final String[] lines =
            StringUtils.splitPreserveAllTokens(this.text, "\n");
        final List<String> pars = new LinkedList<String>();
        for (String line : lines) {
            par.push(line);
            if (par.ready()) {
                pars.add(par.out());
            }
        }
        if (!par.isEmpty()) {
            pars.add(par.out());
        }
        return pars;
    }

    private interface Par {
        /**
         * Add new line to it.
         * @param line The line of text
         */
        void push(String line);
        /**
         * Is it ready?
         * @return TRUE if paragraph is closed
         */
        boolean ready();
        /**
         * Is it empty?
         * @return TRUE if paragraph is empty
         */
        boolean isEmpty();
        /**
         * Get paragraph out of it.
         * @return The text
         */
        String out();
    }

    private abstract static class AbstractPar implements MetaText.Par {
        /**
         * Regular expressions.
         */
        private final transient Map<String, String> regexs;
        /**
         * The text collected so far.
         */
        private final transient StringBuilder text = new StringBuilder();
        /**
         * Line number we're processing now.
         */
        private transient int pos;
        /**
         * PRE-mode is now ON?
         */
        private transient boolean pre;
        /**
         * We're done?
         */
        private transient boolean closed;
        /**
         * Public ctor.
         * @param map Map of regexs
         */
        public AbstractPar(final Map<String, String> map) {
            this.regexs = map;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void push(final String line) {
            final String trimmed = line.trim();
            if ("{{{".equals(trimmed) && !this.pre && this.pos == 0) {
                this.pre = true;
            } else if ("}}}".equals(trimmed) && this.pre) {
                this.closed = true;
            } else if (trimmed.isEmpty() && !this.pre && this.pos != 0) {
                this.closed = true;
            } else {
                this.append(line, trimmed);
            }
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean ready() {
            return this.closed;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEmpty() {
            return this.text.length() == 0;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String out() {
            final String out = this.pack();
            this.closed = false;
            this.pre = false;
            this.text.setLength(0);
            this.pos = 0;
            return out;
        }
        /**
         * Pack text into paragraph.
         * @return The text
         */
        protected abstract String pack();
        /**
         * Get text.
         * @return The text
         */
        protected final String getText() {
            return this.text.toString();
        }
        /**
         * Is in PRE mode?
         * @return True if yes
         */
        protected final boolean isPre() {
            return this.pre;
        }
        /**
         * Format the line.
         * @param line The line to format
         * @return Formatted line
         */
        private String format(final String line) {
            String formatted = line;
            for (Map.Entry<String, String> regex : this.regexs.entrySet()) {
                formatted = formatted.replaceAll(
                    regex.getKey(),
                    regex.getValue()
                );
            }
            return formatted;
        }
        /**
         * Append this line.
         * @param line The line to append
         * @param trimmed Trimmed version of it
         */
        private void append(final String line, final String trimmed) {
            if (this.pos > 0) {
                this.text.append('\n');
            }
            if (this.pre) {
                this.text.append(line);
                ++this.pos;
            } else if (!trimmed.isEmpty()) {
                this.text.append(this.format(trimmed));
                ++this.pos;
            }
        }
    }

}
