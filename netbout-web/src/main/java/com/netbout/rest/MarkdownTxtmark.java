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

import com.github.rjeschke.txtmark.Configuration;
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
        "(?<!\\]\\s{0,256}\\()(?<!\\]:\\s{0,256})(?<!=\")(https?:\\/\\/[a-zA-Z0-9-._~:\\?#@!$&'*+,;=%\\/]+[a-zA-Z0-9-_~#@$&'*+=%\\/])(?![\\w.]*\\]\\()"
    );
    /**
     * Pattern to detect lines which should have a line break at the end.
     * We look for lines which have less than two spaces on their end because
     * TxtMark automatically puts {@code <br/>} if the line has two or more
     * spaces on its end and we should skip a line that starts with four
     * or more spaces because it's a code block in markdown.
     */
    private static final Pattern NEW_LINE = Pattern.compile(
        "^ {0,3}(\\S|(\\S.*\\S)) ?$", Pattern.MULTILINE
    );

    @Override
    public String html(@NotNull final String txt) {
        final Configuration conf = Configuration.builder()
            .enableSafeMode().build();
        return Processor.process(
            MarkdownTxtmark.formatLinks(MarkdownTxtmark.makeLineBreak(txt)),
            conf
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
     * Insert two spaces at the end of string that needs line break to
     * force creation of HTML line break.
     * @param txt Text to replace
     * @return Text with Markdown-formatted links
     */
    private static String makeLineBreak(final String txt) {
        final StringBuffer result = new StringBuffer();
        final Matcher matcher = MarkdownTxtmark.NEW_LINE.matcher(txt);
        while (matcher.find()) {
            if (!matcher.hitEnd()) {
                matcher.appendReplacement(
                    result,
                    String.format(
                        "%s  ", Matcher.quoteReplacement(matcher.group())
                    )
                );
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
