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

import com.github.rjeschke.txtmark.Processor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;

/**
 * Text with markdown formatting.
 * Using TxtMark markdown processor.
 *
 * @author Dmitry Zaytsev (dmitry.zaytsev@gmail.com)
 * @version $Id$
 * @since 2.23
 */
public final class MarkdownTxtmark implements Markdown {
    /**
     * Plain link detection pattern.
     */
    private static final Pattern LINK = Pattern.compile(
        // @checkstyle LineLengthCheck (1 line)
        "(?<!\\]\\()(?<!=\")(https?:\\/\\/[a-zA-Z0-9-._~:\\?#@!$&'*+,;=%\\/]+[a-zA-Z0-9-_~#@$&'*+=%\\/])(?![\\w.]*\\]\\()"
    );
    /**
     * New lines detection pattern.
     */
    private static final Pattern NEW_LINE = Pattern.compile(
        "([^ ]{2}(\\n|\\r\\n)+)"
    );

    @Override
    public String html(@NotNull final String txt) {
        return Processor.process(
            MarkdownTxtmark.formatLinks(MarkdownTxtmark.makeLineBreak(txt))
        );
    }

    /**
     * Replace plain links with Markdown syntax. To be convinced it doesn't
     * replace links inside markdown syntax, it ensures that characters
     * before and after link do not match to Markdown link syntax.
     * @param txt Text to find links in
     * @return Text with Markdown-formatted links
     */
    private static String formatLinks(final String txt) {
        final StringBuffer result = new StringBuffer();
        final Matcher matcher = MarkdownTxtmark.LINK.matcher(txt);
        while (matcher.find()) {
            matcher.appendReplacement(
                result,
                String.format("[%1$s](%1$s)", matcher.group())
            );
        }
        matcher.appendTail(result);
        return result.toString();
    }
    /**
     * Insert two spaces before a new line symbol to force html line break.
     * @param txt Text to replace
     * @return Text with Markdown-formatted links
     */
    private static String makeLineBreak(final String txt) {
        final StringBuffer result = new StringBuffer();
        final Matcher matcher = MarkdownTxtmark.NEW_LINE.matcher(txt);
        while (matcher.find()) {
            matcher.appendReplacement(
                result,
                String.format(
                    "%s  \n",
                    matcher.group().substring(0, matcher.group().length() - 1)
                )
            );
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
