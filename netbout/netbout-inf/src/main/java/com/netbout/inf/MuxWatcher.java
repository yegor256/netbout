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
     * Set of running futures, and their start moments (in msec).
     */
    private final transient ConcurrentMap<Future, Long> running =
        new ConcurrentHashMap<Future, Long>();

    /**
     * Still alive.
     */
    private final transient AtomicBoolean alive = new AtomicBoolean(true);

    /**
     * Public ctor.
     */
    public MuxWatcher() {
        new Thread(this).start();
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
        while (this.alive.get()) {
            this.check();
            try {
                // @checkstyle MagicNumber (1 line)
                TimeUnit.SECONDS.sleep(10L);
            } catch (java.lang.InterruptedException ex) {
                throw new IllegalStateException(ex);
            }
        }
        Logger.info(this, "#run(): finished to watch Mux");
    }

    /**
     * Check all futures.
     */
    private void check() {
        final long threshold = System.currentTimeMillis()
            - TimeUnit.MINUTES.toMillis(2L);
        final long redline = System.currentTimeMillis()
            // @checkstyle MagicNumber (1 line)
            - TimeUnit.SECONDS.toMillis(30L);
        for (Future future : this.running.keySet()) {
            if (future.isDone()) {
                this.running.remove(future);
            } else if (this.running.get(future) < redline) {
                Logger.warn(
                    this,
                    "#check(): one thread is %dms old, looks like a problem",
                    System.currentTimeMillis() - this.running.get(future)
                );
            } else if (this.running.get(future) < threshold) {
                Logger.error(
                    this,
                    "#check(): one thread is %dms old (among %d), killing it",
                    System.currentTimeMillis() - this.running.get(future),
                    this.running.size()
                );
                try {
                    future.get(1L, TimeUnit.SECONDS);
                } catch (java.lang.InterruptedException ex) {
                    throw new IllegalStateException(ex);
                } catch (java.util.concurrent.ExecutionException ex) {
                    throw new IllegalStateException(ex);
                } catch (java.util.concurrent.TimeoutException ex) {
                    future.cancel(true);
                    this.running.remove(future);
                }
            }
        }
        Logger.debug(
            this,
            "#check(): Mux is in good state with %d running tasks",
            this.running.size()
        );
    }

}
