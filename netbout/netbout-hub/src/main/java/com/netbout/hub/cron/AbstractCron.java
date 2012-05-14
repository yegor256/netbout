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
package com.netbout.hub.cron;

import com.jcabi.log.Logger;
import com.netbout.hub.PowerHub;
import java.util.Collection;
import java.util.LinkedList;

/**
 * One cron task.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.DoNotUseThreads")
public abstract class AbstractCron implements Runnable {

    /**
     * The hub.
     */
    private final transient PowerHub ihub;

    /**
     * Public ctor.
     * @param hub The hub
     */
    public AbstractCron(final PowerHub hub) {
        this.ihub = hub;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public final void run() {
        final long start = System.nanoTime();
        try {
            this.cron();
        // @checkstyle IllegalCatch (1 line)
        } catch (Exception ex) {
            Logger.error(this, "#run(): %[exception]s", ex);
        }
        Logger.debug(
            this,
            "#run(): %[type]s done in %[nano]s",
            this,
            System.nanoTime() - start
        );
    }

    /**
     * Instantiate all of them.
     * @param hub The hub to use
     * @return List of them
     */
    public static Collection<Runnable> all(final PowerHub hub) {
        final Collection<Runnable> tasks = new LinkedList<Runnable>();
        tasks.add(new Reminder(hub));
        tasks.add(new Routine(hub));
        tasks.add(new Indexer(hub));
        return tasks;
    }

    /**
     * Run it.
     * @throws Exception If any problem insde
     */
    protected abstract void cron() throws Exception;

    /**
     * Get access to incapsulated Hub.
     * @return The hub
     */
    protected final PowerHub hub() {
        return this.ihub;
    }

}
