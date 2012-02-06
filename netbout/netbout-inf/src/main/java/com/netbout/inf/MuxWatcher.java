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
package com.netbout.inf;

import com.ymock.util.Logger;
import java.io.Closeable;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Multiplexer of heap updating tasks.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.DoNotUseThreads")
final class MuxWatcher implements Closeable, Runnable {

    /**
     * Maximum task execution time, in msec.
     */
    private static final long MAX_TIME = 8 * 60 * 1000L;

    /**
     * When we should issue a warning about the task, in msec.
     */
    private static final long WARN_TIME = 2 * 60 * 1000L;

    /**
     * How often to check, in msec.
     */
    private static final long CHECK_TIME = 20 * 1000L;

    /**
     * Maximum warnings to show.
     */
    private static final long MAX_WARNINGS = 10;

    /**
     * Set of running futures, and their start moments (in msec).
     */
    private final transient ConcurrentMap<Future, Long> running =
        new ConcurrentHashMap<Future, Long>();

    /**
     * Still alive.
     */
    private final transient AtomicBoolean alive = new AtomicBoolean(false);

    /**
     * Public ctor.
     */
    public MuxWatcher() {
        new Thread(this).start();
    }

    /**
     * Show some stats.
     * @param The text
     */
    public String stats() {
        final StringBuilder text = new StringBuilder();
        text.append(String.format("%d running futures", this.running.size()));
        return text.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        this.alive.set(false);
        Logger.info(this, "#close(): no more watching");
    }

    /**
     * Add one more task to watching list.
     * @param future The future
     */
    public void watch(final Future future) {
        this.running.put(future, System.currentTimeMillis());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        Logger.info(this, "#run(): starting to watch Mux");
        this.alive.set(true);
        while (this.alive.get()) {
            this.futures();
            this.deadlocks();
            try {
                TimeUnit.MILLISECONDS.sleep(this.CHECK_TIME);
            } catch (InterruptedException ex) {
                throw new IllegalStateException(ex);
            }
        }
        Logger.info(this, "#run(): finished to watch Mux");
    }

    /**
     * Check all futures.
     */
    private void futures() {
        final List<String> warnings = new ArrayList<String>();
        for (Future future : this.running.keySet()) {
            final String warning = this.check(future);
            if (!warning.isEmpty() && warnings.size() < this.MAX_WARNINGS) {
                warnings.add(warning);
            }
        }
        if (!warnings.isEmpty()) {
            Logger.warn(
                this,
                "#futures(): some potential problems: %[list]s",
                warnings
            );
        }
        Logger.debug(
            this,
            "#futures(): Mux is in good state with %d running tasks",
            this.running.size()
        );
    }

    /**
     * Check for deadlocks.
     */
    private void deadlocks() {
        final ThreadMXBean tmx = ManagementFactory.getThreadMXBean();
        final long[] threads = tmx.findDeadlockedThreads();
        if (threads == null) {
            Logger.debug(this, "#deadlocks(): no deadlocks found");
        } else {
            final ThreadInfo[] infos = tmx.getThreadInfo(threads, true, true);
            for (ThreadInfo info : infos) {
                Logger.error(
                    this,
                    "#deadlocks(): thread in deadlock: %s",
                    info
                );
            }
        }
    }

    /**
     * Check one future.
     * @param future The future to check
     * @return Warning, if necessary
     */
    private String check(final Future future) {
        String warning = "";
        final long age = System.currentTimeMillis()
            - this.running.get(future);
        if (future.isDone()) {
            this.running.remove(future);
        } else if (age > this.MAX_TIME) {
            Logger.error(
                this,
                "#check(%s): %dms old (among %d), killing it",
                future,
                age,
                this.running.size()
            );
            try {
                future.get(1L, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                throw new IllegalStateException(ex);
            } catch (java.util.concurrent.ExecutionException ex) {
                throw new IllegalStateException(ex);
            } catch (java.util.concurrent.TimeoutException ex) {
                future.cancel(true);
                this.running.remove(future);
            }
        } else if (age > this.WARN_TIME) {
            warning = String.format("%s: %dms", future, age);
        }
        return warning;
    }

}
