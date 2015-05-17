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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.log.Logger;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.IOUtils;
import org.pegdown.PegDownProcessor;
import org.w3c.tidy.Tidy;

/**
 * Text with markdown formatting.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @see <a href="Markdown Syntax">http://daringfireball.net/projects/markdown/syntax</a>
 */
@Immutable
public final class Markdown {

    /**
     * Tidy.
     */
    private static final Tidy TIDY = Markdown.makeTidy();

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
     * @link https://github.com/sirthias/pegdown/issues/136
     */
    @RetryOnFailure(verbose = false)
    public String html() {
        synchronized (Markdown.TIDY) {
            return Markdown.clean(
                new PegDownProcessor().markdownToHtml(this.text)
            );
        }
    }

    /**
     * Clean the XML.
     * @param xml The XML to clean
     * @return Clean XML
     */
    private static String clean(final String xml) {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Markdown.TIDY.parse(
                IOUtils.toInputStream(xml, CharEncoding.UTF_8),
                baos
            );
            return baos.toString(CharEncoding.UTF_8);
        } catch (final IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    /**
     * Make and return a configured Tidy.
     * @return The Tidy
     * @checkstyle ExecutableStatementCountCheck (50 lines)
     */
    private static Tidy makeTidy() {
        final Tidy tidy = new Tidy();
        tidy.setShowErrors(0);
        tidy.setErrout(
            new PrintWriter(Logger.stream(Level.FINE, Markdown.class))
        );
        tidy.setUpperCaseTags(false);
        tidy.setUpperCaseAttrs(false);
        tidy.setLowerLiterals(false);
        tidy.setIndentContent(false);
        tidy.setDropProprietaryAttributes(false);
        tidy.setBreakBeforeBR(false);
        tidy.setShowWarnings(true);
        tidy.setXmlTags(true);
        tidy.setXmlSpace(false);
        tidy.setEncloseBlockText(true);
        tidy.setNumEntities(true);
        tidy.setDropEmptyParas(true);
        tidy.setFixBackslash(true);
        tidy.setFixComments(true);
        tidy.setInputEncoding(CharEncoding.UTF_8);
        tidy.setOutputEncoding(CharEncoding.UTF_8);
        tidy.setSmartIndent(false);
        tidy.setFixUri(true);
        tidy.setForceOutput(true);
        return tidy;
    }

}
