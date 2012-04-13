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

import com.netbout.spi.Message;
import com.netbout.spi.Participant;
import com.netbout.spi.Urn;
import com.ymock.util.Logger;
import java.util.HashSet;
import java.util.Set;

/**
 * The task to review one notice.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class SeeTask implements Task {

    /**
     * The store with predicates.
     */
    private final transient Store store;

    /**
     * When started.
     */
    private transient long started;

    /**
     * When finished (or ZERO if still running).
     */
    private transient long finished;

    /**
     * The notice.
     */
    private final transient Notice notice;

    /**
     * The listener.
     */
    private final transient TaskListener listener;

    /**
     * Dependants.
     */
    private final transient Set<Urn> deps = new HashSet<Urn>();

    /**
     * Public ctor.
     * @param what The notice to process
     * @param store The store to use
     * @param ltr Listener of result
     */
    public SeeTask(final Notice what, final Store store,
        final TaskListener ltr) {
        this.istore = store;
        this.notice = what;
        this.listener = ltr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object task) {
        return task instanceof Task && this.hashCode() == task.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        this.started = System.nanoTime();
        this.execute();
        this.finished = System.nanoTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long time() {
        long time;
        if (this.finished == 0L) {
            time = System.nanoTime() - this.started;
        } else {
            time = this.finished - this.started;
        }
        return time;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Urn> dependants() {
        return this.deps;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("see-message-#%d", this.message.number());
    }

    /**
     * Execute it.
     * <p>There is no synchronization, intentionally. Msg class is thread-safe
     * and we don't worry about concurrent changes to it.
     */
    private void execute() {
        this.store().see(this.message);
        Logger.debug(
            this,
            "#execute(): cached message #%d in %[nano]s",
            this.message.number(),
            this.time()
        );
    }

}
