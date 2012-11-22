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
package com.netbout.inf;

import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import java.io.Closeable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Factory of {@link Runnable}-s that are being watched.
 *
 * <p>This whole class is a response to a defect in JDK7,
 * see: http://stackoverflow.com/questions/12349881
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.DoNotUseThreads")
final class PatronizedRunnables implements Closeable {

    /**
     * Threshold in milliseconds.
     */
    private final transient long threshold;

    /**
     * When execution was started in each runnable.
     */
    private final transient ConcurrentMap<Thread, Long> started =
        new ConcurrentHashMap<Thread, Long>();

    /**
     * Running service.
     */
    private final transient ScheduledExecutorService service =
        Executors.newSingleThreadScheduledExecutor(new VerboseThreads());

    /**
     * Public ctor.
     * @param thr The threshold in milliseconds
     */
    public PatronizedRunnables(final long thr) {
        this.threshold = thr;
        this.service.scheduleWithFixedDelay(
            new VerboseRunnable(
                new Runnable() {
                    @Override
                    public void run() {
                        PatronizedRunnables.this.patronize();
                    }
                }
            ),
            1L, 1L, TimeUnit.SECONDS
        );
    }

    /**
     * Patronize this runnable.
     * @param runnable The runnable to patronize
     * @return New one, being patronized
     */
    public Runnable patronize(final Runnable runnable) {
        return new Runnable() {
            @Override
            public void run() {
                PatronizedRunnables.this.started.put(
                    Thread.currentThread(),
                    System.currentTimeMillis()
                );
                try {
                    runnable.run();
                } finally {
                    PatronizedRunnables.this.started.remove(
                        Thread.currentThread()
                    );
                }
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        this.service.shutdown();
    }

    /**
     * Patronize them all.
     */
    private void patronize() {
        final Collection<String> slow = new LinkedList<String>();
        for (Thread thread : this.started.keySet()) {
            final Long start = this.started.get(thread);
            if (start == null) {
                continue;
            }
            final long age = System.currentTimeMillis() - start;
            if (age > this.threshold) {
                slow.add(
                    Logger.format(
                        "%s/%s over %[ms]s",
                        thread.getName(),
                        thread.getState(),
                        age
                    )
                );
            }
            // @checkstyle MagicNumber (1 line)
            if (age > 15 * 1000
                && thread.getState().equals(Thread.State.WAITING)) {
                Logger.warn(
                    this,
                    "#patronize(): %s/%s is %[ms]s old, interrupting:\n%s",
                    thread.getName(),
                    thread.getState(),
                    age,
                    PatronizedRunnables.stack(thread.getStackTrace())
                );
                thread.interrupt();
            }
        }
        if (!slow.isEmpty()) {
            Logger.debug(
                this,
                "#patronize(): %d of %d threads are slow: %[list]s",
                slow.size(),
                this.started.size(),
                slow
            );
        }
    }

    /**
     * Convert array of stack trace elements to string.
     * @param elements The elemetns
     * @return The string
     */
    private static String stack(final StackTraceElement[] elements) {
        final StringBuilder text = new StringBuilder();
        for (StackTraceElement element : elements) {
            text.append('\t').append(element.toString()).append('\n');
        }
        return text.toString();
    }

}
