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

/**
 * Abstract task.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
abstract class AbstractTask implements Task {

    /**
     * When started.
     */
    private transient long started;

    /**
     * When finished (or NULL if still running).
     */
    private transient long finished;

    /**
     * The index to work with.
     */
    private final transient Index data;

    /**
     * Public ctor.
     * @param index The index to work with
     */
    public AbstractTask(final Index index) {
        this.data = index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        return this.toString().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(final Object task) {
        return this.hashCode() == task.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void run() {
        this.started = System.nanoTime();
        this.execute();
        this.finished = System.nanoTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final long time() {
        long time;
        if (this.finished == 0L) {
            time = System.nanoTime() - this.started;
        } else {
            time = this.finished - this.started;
        }
        return time;
    }

    /**
     * Execute task.
     */
    protected abstract void execute();

    /**
     * Get index.
     * @return The index
     */
    protected final Index index() {
        return this.data;
    }

}
