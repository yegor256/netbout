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
package com.netbout.rest.meta;

import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 * Text with meta commands.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
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
        return StringUtils.join(this.paragraphs(new HtmlPar()), "");
    }

    /**
     * Convert it to plain text.
     * @return The plain text
     */
    public String plain() {
        return StringUtils.join(this.paragraphs(new PlainPar()), "\n\n");
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

}
