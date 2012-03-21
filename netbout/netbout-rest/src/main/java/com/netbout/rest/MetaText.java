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
@SuppressWarnings("PMD.CyclomaticComplexity")
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
     * @checkstyle MultipleStringLiterals (10 lines)
     */
    public String html() {
        return this.reformat(
            ArrayUtils.toMap(
                new Object[][] {
                    {"\\[(.*?)\\]\\((http://.*?)\\)", "<a href='$2'>$1</a>"},
                    {"\\*+(.*?)\\*+", "<b>$1</b>"},
                    {"`(.*?)`", "<span class='tt'>$1</span>"},
                    {"_+(.*?)_+", "<i>$1</i>"},
                }
            )
        );
    }

    /**
     * Convert it to plain text.
     * @return The plain text
     * @checkstyle MultipleStringLiterals (10 lines)
     */
    public String plain() {
        return this.reformat(
            ArrayUtils.toMap(
                new Object[][] {
                    {"\\[(.*?)\\]\\((http://.*?)\\)", "$1 ($2)"},
                    {"\\*\\*(.*?)\\*\\*", "$1"},
                    {"`(.*?)`", "$1"},
                    {"_(.*?)_", "$1"},
                }
            )
        );
    }

    /**
     * Reformat, using regular expressions.
     * @param regexs Regular expressions
     * @return Reformatted text
     * @checkstyle CyclomaticComplexity (60 lines)
     * @checkstyle ExecutableStatementCount (60 lines)
     */
    @SuppressWarnings("PMD.NPathComplexity")
    private String reformat(final Map<String, String> regexs) {
        boolean par = false;
        boolean pre = false;
        final String[] lines = StringUtils.splitPreserveAllTokens(
            this.text,
            "\n"
        );
        final StringBuilder output = new StringBuilder();
        for (int pos = 0; pos < lines.length; ++pos) {
            String line = lines[pos].trim();
            if ("{{{".equals(line) && !pre) {
                pre = true;
                continue;
            }
            if ("}}}".equals(line) && pre) {
                pre = false;
                continue;
            }
            if (line.isEmpty() && !pre) {
                if (par) {
                    output.append("</p>");
                }
                par = false;
                continue;
            }
            if (pre) {
                line = lines[pos];
            } else {
                line = this.reformat(line, regexs);
            }
            if (par) {
                output.append('\n');
            } else {
                output.append(this.parStart(pre));
                par = true;
            }
            output.append(line);
        }
        if (par) {
            output.append("<!-- end of text --></p>");
        }
        return output.toString();
    }

    /**
     * Reformat one line, using regular expressions.
     * @param line The line to reformat
     * @param regexs Regular expressions
     * @return Reformatted line
     */
    private String reformat(final String line,
        final Map<String, String> regexs) {
        String parsed = line;
        for (Map.Entry<String, String> regex : regexs.entrySet()) {
            parsed = parsed.replaceAll(
                regex.getKey(),
                regex.getValue()
            );
        }
        return parsed;
    }

    /**
     * Create PAR starting line.
     * @param pre Is it PRE mode?
     * @return Par starting line
     */
    private String parStart(final boolean pre) {
        String line;
        if (pre) {
            line = "<p class='fixed'>";
        } else {
            line = "<p>";
        }
        return line;
    }

}
