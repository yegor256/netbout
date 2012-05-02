/**
 * Copyright (c) 2009-2012, Netbout.com
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
package com.netbout.rest.log;

import com.netbout.spi.text.SecureString;
import com.jcabi.log.Logger;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 * List of log events.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class LogList {

    /**
     * Separator.
     */
    private static final String SEP = "><";

    /**
     * List of log events.
     */
    private final transient List<String> list = new LinkedList<String>();

    /**
     * Public ctor.
     */
    public LogList() {
        WebAppender.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        for (Object event : ListUtils.union(this.list, WebAppender.get())) {
            text.append(StringEscapeUtils.escapeXml(event.toString()))
                .append(this.SEP);
        }
        return new SecureString(text.toString()).toString();
    }

    /**
     * Append events from text.
     * @param text The text
     */
    public void append(final String text) {
        try {
            final String[] events = StringUtils.splitPreserveAllTokens(
                SecureString.valueOf(text).text(),
                this.SEP
            );
            for (String event : events) {
                this.list.add(StringEscapeUtils.unescapeXml(event));
            }
        } catch (com.netbout.spi.text.StringDecryptionException ex) {
            Logger.warn(this, "#append(%s): %[exception]s", text, ex);
        }
    }

    /**
     * Get list of events.
     * @return The list
     */
    public List<String> events() {
        return ListUtils.union(this.list, WebAppender.get());
    }

    /**
     * Clear the list.
     */
    public void clear() {
        this.list.clear();
    }

}
