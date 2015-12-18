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
        "https?://[a-zA-Z0-9-._~:/\\?#@!$&'*+,;=%]+[a-zA-Z0-9-_~/#@$&'*+=%]"
    );

    @Override
    public String html(@NotNull final String txt) {
        return Processor.process(MarkdownTxtmark.formatLinks(txt));
    }

    /**
     * Replace plain links with Markdown syntax. To make sure it doesn't
     * replace links inside markdown syntax, it makes sure that characters
     * before and after link do not match to Markdown link syntax.
     * @param txt Text to find links in
     * @return Text with Markdown-formatted links
     */
    private static String formatLinks(final String txt) {
        final String marker = "](";
        final String html = "=\"";
        final StringBuilder result = new StringBuilder();
        final Matcher matcher = MarkdownTxtmark.LINK.matcher(txt);
        int start = 0;
        while (matcher.find(start)) {
            result.append(txt.substring(start, matcher.start()));
            final String prefix = txt.substring(
                Math.max(0, matcher.start() - 2),
                matcher.start()
            );
            final String suffix = txt.substring(
                matcher.end(),
                Math.min(txt.length(), matcher.end() + 2)
            );
            final String uri = matcher.group();
            if (marker.equals(suffix) || marker.equals(prefix)
                || html.equals(prefix)) {
                result.append(uri);
            } else {
                result.append(String.format("[%1$s](%1$s)", uri));
            }
            start = matcher.end();
        }
        result.append(txt.substring(start));
        return result.toString();
    }
}
