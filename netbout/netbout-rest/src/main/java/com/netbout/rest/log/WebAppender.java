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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Global web log appender.
 *
 * <p>This class is used by LOG4J and is configured in
 * {@code log4j.properties}. It just saves all log events into a static
 * variable and holds them there forever, until {@link #start()} is
 * called. Actually, this class only consumes events and let {@link LogList} to
 * retrieve them when necessary.
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
        final boolean suites =
            WebAppender.EVENTS.containsKey(event.getThreadName())
            && event.getLevel().isGreaterOrEqual(Level.INFO)
            && !((String) event.getMessage()).isEmpty()
            && ((String) event.getMessage()).charAt(0) != '#';
        if (suites) {
            WebAppender.EVENTS.get(event.getThreadName()).add(
                this.getLayout().format(event)
            );
        }
    }

    /**
     * Start recording events for this thread (called only by
     * {@link LogList} ctor).
     *
     * @see LogList#LogList
     */
    @SuppressWarnings("PMD.DefaultPackage")
    static void start() {
        WebAppender.EVENTS.put(
            Thread.currentThread().getName(),
            new LinkedList<String>()
        );
    }

    /**
     * Get all recorded events, for current thread.
     * @return The list of events
     * @see LogList#events()
     */
    @SuppressWarnings("PMD.DefaultPackage")
    static List<String> get() {
        return WebAppender.EVENTS.get(Thread.currentThread().getName());
    }

}
