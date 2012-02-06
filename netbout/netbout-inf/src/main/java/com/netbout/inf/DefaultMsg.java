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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

/**
 * Default implementation of {@link Msg}.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class DefaultMsg implements Msg {

    /**
     * Indicator of message readiness for reading.
     */
    private final transient CountDownLatch latch = new CountDownLatch(1);

    /**
     * Number of message.
     */
    private final transient Long num;

    /**
     * Number of bout.
     */
    private final transient Long bnum;

    /**
     * All properties.
     */
    private final transient Collection<Pair> pairs = new ArrayList<Pair>();

    /**
     * Public ctor.
     * @param msg Number of message
     * @param bout Number of bout
     */
    public DefaultMsg(final Long msg, final Long bout) {
        this.num = msg;
        this.bnum = bout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long number() {
        return this.num;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long bout() {
        return this.bnum;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(final String name) {
        this.awaitCompletion();
        T found = null;
        for (Pair pair : this.pairs) {
            if (pair.getKey().equals(name)) {
                found = (T) pair.getValue();
                break;
            }
        }
        if (found == null) {
            throw new IllegalArgumentException(
                String.format(
                    "property '%s' not found in Msg #%d",
                    name,
                    this.num
                )
            );
        }
        return found;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> boolean has(final String name, final T value) {
        this.awaitCompletion();
        boolean has = false;
        for (Pair pair : this.pairs) {
            if (pair.getKey().equals(name) && pair.getValue().equals(value)) {
                has = true;
                break;
            }
        }
        return has;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> void put(final String name, final T value) {
        synchronized (this) {
            if (this.latch.getCount() == 0) {
                throw new IllegalStateException(
                    String.format(
                        "can't put('%s') to Msg #%d since it's closed already",
                        name,
                        this.num
                    )
                );
            }
            this.pairs.add(new Pair(name, value));
        }
    }

    /**
     * Close the object and disallow any more {@code put()} requests.
     * @see SeeMessageTask#exec()
     */
    public void close() {
        synchronized (this) {
            if (this.latch.getCount() == 0) {
                throw new IllegalStateException(
                    String.format(
                        "can't close() Msg #%d since it's already closed",
                        this.num
                    )
                );
            }
            this.latch.countDown();
        }
    }

    /**
     * The pair to work with.
     */
    private static final class Pair
        extends AbstractMap.SimpleEntry<String, Object> {
        /**
         * Public ctor.
         * @param name The name
         * @param value The value
         */
        public Pair(final String name, final Object value) {
            super(name, value);
        }
    }

    /**
     * Wait until the class is ready to return data.
     */
    private void awaitCompletion() {
        try {
            this.latch.await();
        } catch (InterruptedException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

}
