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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
    private static final long MAX_TIME = 15 * 60 * 1000L;

    /**
     * When we should issue a warning about the task, in msec.
     */
    private static final long WARN_TIME = 2 * 60 * 1000L;

    /**
     * How often to check, in msec.
     */
    private static final long CHECK_TIME = 60 * 1000L;

    /**
     * Maximum warnings to show.
     */
    private static final long MAX_WARNINGS = 2;

    /**
     * Set of running threads, and their start moments (in msec).
     */
    private final transient ConcurrentMap<Thread, Long> threads =
        new ConcurrentHashMap<Thread, Long>();

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
     * @return The text
     */
    public String statistics() {
        final StringBuilder text = new StringBuilder();
        text.append(String.format("%d watched threads", this.threads.size()));
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
     * Add one more thread to watching list.
     * @param thread The thread
     * @param task The task
     */
    public void started(final Thread thread, final Task task) {
        // todo...
    }

    /**
     * This thread just finished execution of a task.
     * @param task The task just finished
     */
    public void finished(final Task task) {
        // todo...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        Logger.info(this, "#run(): starting to watch Mux");
        this.alive.set(true);
        while (this.alive.get()) {
            this.deadlocks();
            try {
                this.watch();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        Logger.info(this, "#run(): finished to watch Mux");
    }

    /**
     * Check one task.
     * @throws InterruptedException Once interrupted
     */
    private void watch() throws InterruptedException {
        // String warning = "";
        // final long age = System.currentTimeMillis()
        //     - this.running.get(task);
        // if (task.isDone()) {
        //     this.running.remove(task);
        // } else if (age > this.MAX_TIME) {
        //     Logger.error(
        //         this,
        //         "#check(%s): %dms old (among %d)",
        //         task,
        //         age,
        //         this.running.size()
        //     );
        // } else if (age > this.WARN_TIME) {
        //     warning = String.format("older than %dms", age);
        // }
        // return warning;
    }

    /**
     * Check for deadlocks.
     */
    private void deadlocks() {
        final ThreadMXBean tmx = ManagementFactory.getThreadMXBean();
        final long[] ids = tmx.findDeadlockedThreads();
        if (ids == null) {
            Logger.debug(this, "#deadlocks(): no deadlocks found");
        } else {
            final ThreadInfo[] infos = tmx.getThreadInfo(ids, true, true);
            for (ThreadInfo info : infos) {
                Logger.error(
                    this,
                    "#deadlocks(): thread in deadlock: %s",
                    info
                );
            }
        }
    }

}
