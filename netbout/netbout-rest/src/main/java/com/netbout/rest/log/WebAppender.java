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
package com.netbout.rest.log;

import com.netbout.text.SecureString;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Global web log appender.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class WebAppender extends AppenderSkeleton {

    /**
     * List of log events (keys are thread names).
     */
    private static final ConcurrentMap<String, List<String>> EVENTS =
        new ConcurrentHashMap<String, List<String>>();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requiresLayout() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activateOptions() {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void append(final LoggingEvent event) {
        if (this.EVENTS.containsKey(event.getThreadName())) {
            this.EVENTS.get(event.getThreadName()).add(
                this.getLayout().format(event)
            );
        }
    }

    /**
     * Start recording events for this thread.
     */
    public static void start() {
        WebAppender.EVENTS.put(
            Thread.currentThread().getName(),
            new LinkedList<String>()
        );
    }

    /**
     * Get all recorded events, for current thread.
     * @return The list of events
     */
    public static List<String> get() {
        return WebAppender.EVENTS.get(Thread.currentThread().getName());
    }

}
