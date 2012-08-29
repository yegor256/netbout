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
package com.netbout.inf;

import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Factory of {@link Runnable}-s that are being watched.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id: Mux.java 3289 2012-08-28 15:02:31Z yegor@tpc2.com $
 */
@SuppressWarnings("PMD.DoNotUseThreads")
final class PatronizedThreads implements ThreadFactory {

    /**
     * Factory of verbose threads.
     */
    private final transient ThreadFactory factory;

    /**
     * When execution was started in each thread/runnable.
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
     * @param label Label for threads
     */
    public PatronizedThreads(final String label) {
        this.factory = new VerboseThreads(label);
        this.service.scheduleWithFixedDelay(
            new VerboseRunnable(
                new Runnable() {
                    @Override
                    public void run() {
                        PatronizedThreads.this.patronize();
                    }
                }
            ),
            1L, 1L, TimeUnit.SECONDS
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Thread newThread(final Runnable runnable) {
        final AtomicReference<Thread> ref = new AtomicReference<Thread>();
        ref.set(
            this.factory.newThread(
                new Runnable() {
                    @Override
                    public void run() {
                        PatronizedThreads.this.started.put(
                            ref.get(),
                            System.currentTimeMillis()
                        );
                        try {
                            runnable.run();
                        } finally {
                            PatronizedThreads.this.started.remove(ref.get());
                        }
                    }
                }
            )
        );
        Logger.debug(this, "#newThread(): done");
        return ref.get();
    }

    /**
     * Patronize them all.
     */
    private void patronize() {
        // @checkstyle MagicNumber (1 line)
        final long threshold = System.currentTimeMillis() - 500;
        int slow = 0;
        for (ConcurrentMap.Entry<Thread, Long> entry
            : this.started.entrySet()) {
            if (entry.getValue() < threshold) {
                ++slow;
            }
        }
        if (slow > 0) {
            Logger.warn(this, "#patronize(): %d slow threads", slow);
        }
    }

}
