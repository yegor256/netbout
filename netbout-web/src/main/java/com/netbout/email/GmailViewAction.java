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
     * Returns the HTML for Gmail ViewAction.
     * @return The HTML
     * @checkstyle LineLength (15 lines)
     */
    @SuppressWarnings("PMD.ConsecutiveLiteralAppends")
    public String html() {
        final StringBuilder html = new StringBuilder(540);
        html.append("<div itemscope itemtype=\"http://schema.org/EmailMessage\">")
            .append("  <meta itemprop=\"description\" content=\"View Bout\"/>")
            .append("  <div itemprop=\"potentialAction\" itemscope itemtype=\"http://schema.org/ViewAction\">")
            .append("    <link itemprop=\"target\" href=\"http://www.netbout.com/b/%d\"/>")
            .append("    <meta itemprop=\"name\" content=\"View Bout\"/>")
            .append("  </div>")
            .append(" <div itemprop=\"publisher\" itemscope itemtype=\"http://schema.org/Organization\">")
            .append("  <meta itemprop=\"name\" content=\"Netbout\"/>")
            .append("  <link itemprop=\"url\" href=\"http://www.netbout.com/\"/>")
            .append(" </div>")
            .append("</div>");
        return String.format(
            html.toString(),
            this.number
        );
    }
}
