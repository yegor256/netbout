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

import com.netbout.spi.Urn;
import com.ymock.util.Logger;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Multiplexer of heap updating tasks.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.DoNotUseThreads")
final class Mux {

    /**
     * Executor service.
     */
    private final transient ExecutorService executor =
        Executors.newFixedThreadPool(10);

    /**
     * How many tasks are currently waiting.
     */
    private final transient ConcurrentMap<Urn, AtomicLong> waiting =
        new ConcurrentHashMap<Urn, AtomicLong>();

    /**
     * How long do I need to wait before sending requests?
     * @param who Who is asking
     * @return Estimated number of milliseconds
     */
    public Long eta(final Urn who) {
        return this.waiting.get(who).get();
    }

    /**
     * Add new task to be executed ASAP.
     * @param who Who is waiting for this task
     * @param task The task to execute
     */
    @SuppressWarnings({
        "PMD.AvoidInstantiatingObjectsInLoops", "PMD.AvoidCatchingThrowable"
    })
    public void submit(final Collection<Urn> who, final Task task) {
        synchronized (this) {
            for (Urn urn : who) {
                if (!this.waiting.containsKey(who)) {
                    this.waiting.put(urn, new AtomicLong());
                }
                this.waiting.get(urn).incrementAndGet();
            }
        }
        this.executor.submit(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        task.exec();
                        for (Urn urn : who) {
                            Mux.this.waiting.get(urn).decrementAndGet();
                        }
                    // @checkstyle IllegalCatchCheck (1 line)
                    } catch (Throwable ex) {
                        Logger.error(
                            this,
                            "run(): %[exception]s",
                            ex
                        );
                    }
                }
            }
        );
    }

}
