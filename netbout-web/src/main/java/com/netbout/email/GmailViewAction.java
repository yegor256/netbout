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
package com.netbout.email;

import org.xembly.Directives;
import org.xembly.ImpossibleModificationException;
import org.xembly.Xembler;

/**
 * Integrates with Gmail's View Actions.
 * @author Matteo Barbieri (barbieri.matteo@gmail.com)
 * @version $Id$
 */
final class GmailViewAction {

    /**
     * The bout number.
     */
    private final transient long number;

    /**
     * Primary ctor.
     * @param nbr The bout number
     */
    GmailViewAction(final long nbr) {
        this.number = nbr;
    }

    /**
     * Returns the XML section for Gmail ViewAction.
     * @return The XML
     * @checkstyle MultipleStringLiteralsCheck (45 lines)
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public String xml() {
        try {
            return new Xembler(
                new Directives()
                    .add("div")
                    .attr("itemscope", "")
                    .attr("itemtype", "http://schema.org/EmailMessage")
                    .add("meta")
                    .attr("itemprop", "description")
                    .attr("content", "View Bout").up()
                    .add("div")
                    .attr("itemprop", "potentialAction")
                    .attr("itemscope", "")
                    .attr("itemtype", "http://schema.org/ViewAction")
                    .add("link").attr("itemprop", "target")
                    .attr(
                        "href",
                        String.format(
                            "http://www.netbout.com/b/%d", this.number
                        )
                    ).up()
                    .add("link").attr("itemprop", "url")
                    .attr(
                        "href",
                        String.format(
                            "http://www.netbout.com/b/%d", this.number
                        )
                    ).up()
                    .add("meta")
                    .attr("itemprop", "name")
                    .attr("content", "View Bout").up().up()
                    .add("div")
                    .attr("itemprop", "publisher")
                    .attr("itemscope", "")
                    .attr("itemtype", "http://schema.org/Organization")
                    .add("meta")
                    .attr("itemprop", "name")
                    .attr("content", "Netbout").up()
                    .add("link")
                    .attr("itemprop", "url")
                    .attr("href", "http://www.netbout.com")
            ).xml();
        } catch (final ImpossibleModificationException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
