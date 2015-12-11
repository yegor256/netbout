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

import javax.validation.constraints.NotNull;

/**
 * Text with markdown formatting.
 *
 * @author Dmitry Zaytsev (dmitry.zaytsev@gmail.com)
 * @version $Id$
 * @see <a href="Markdown Syntax">http://daringfireball.net/projects/markdown/syntax</a>
 * @todo #847:30min/DEV MarkdownTxtmark not implemented but has to be.
 *  This class should be implementation of Markdown using TxtMark.
 *  See https://github.com/rjeschke/txtmark. Don't forget about unit tests.
 */
public interface Markdown {
    /**
     * Convert it to HTML.
     * @param txt The raw source text, with meta commands
     * @return The HTML
     * @link https://github.com/sirthias/pegdown/issues/136
     */
    String html(@NotNull String txt);

    /**
     * Default implementation.
     */
    final class Default implements Markdown {
        /**
         * Markdown processor.
         */
        private final Markdown processor;

        /**
         * Ctor.
         */
        public Default() {
            this.processor = new MarkdownPegdown();
        }

        @Override
        public String html(@NotNull final String txt) {
            return this.processor.html(txt);
        }
    }
}
