/**
 * Copyright (c) 2009-2014, netbout.com
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

import com.jcabi.aspects.Immutable;
import java.io.StringReader;
import java.io.StringWriter;
import javax.validation.constraints.NotNull;
import org.tautua.markdownpapers.parser.ParseException;

/**
 * Text with markdown formatting.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @see <a href="Markdown Syntax">http://daringfireball.net/projects/markdown/syntax</a>
 */
@Immutable
public final class Markdown {

    /**
     * The source text.
     */
    private final transient String text;

    /**
     * Public ctor.
     * @param txt The raw source text, with meta commands
     */
    public Markdown(@NotNull final String txt) {
        this.text = txt;
    }

    /**
     * Convert it to HTML.
     * @return The HTML
     */
    public String html() {
        final StringWriter writer = new StringWriter();
        try {
            new org.tautua.markdownpapers.Markdown().transform(
                new StringReader(this.text),
                writer
            );
        } catch (final ParseException ex) {
            throw new IllegalArgumentException(ex);
        }
        return writer.toString();
    }

    /**
     * Convert it to plain text.
     * @return The plain text
     */
    public String plain() {
        return this.html();
    }

}
