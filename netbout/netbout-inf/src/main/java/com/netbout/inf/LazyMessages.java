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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Lazy list of message numbers.
 *
 * <p>It's thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class LazyMessages implements Iterable<Long> {

    /**
     * Time out interval in milliseconds.
     */
    private static final long TIMEOUT = 5000;

    /**
     * The ray.
     */
    private final transient Ray ray;

    /**
     * The term.
     */
    private final transient Term term;

    /**
     * Public ctor.
     * @param iray The ray to use
     * @param trm The term
     */
    public LazyMessages(final Ray iray, final Term trm) {
        this.ray = iray;
        this.term = trm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Long> iterator() {
        return new MessagesIterator(this.ray.cursor());
    }

    /**
     * Iterator.
     *
     * <p>The class is thread-safe.
     */
    private final class MessagesIterator implements Iterator<Long> {
        /**
         * When the iterator was started.
         */
        private final transient Long start = System.currentTimeMillis();
        /**
         * Cursor to use.
         */
        private final transient AtomicReference<Cursor> cursor;
        /**
         * Shifted already?
         */
        private final transient AtomicBoolean shifted = new AtomicBoolean();
        /**
         * Public ctor.
         * @param crs The cursor
         */
        public MessagesIterator(final Cursor crs) {
            this.cursor = new AtomicReference<Cursor>(crs);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            synchronized (this.start) {
                if (!this.shifted.get()) {
                    final Cursor next =
                        this.cursor.get().shift(LazyMessages.this.term);
                    if (next.compareTo(this.cursor.get()) >= 0) {
                        throw new IllegalStateException(
                            String.format(
                                "%s shifted to %s by %s, wrong way",
                                this.cursor,
                                next,
                                LazyMessages.this.term
                            )
                        );
                    }
                    this.cursor.set(next);
                    this.shifted.set(true);
                }
                boolean has;
                if (System.currentTimeMillis() - this.start
                    > LazyMessages.TIMEOUT) {
                    Logger.warn(
                        this,
                        "#hasNext(): slow iterator at '%s', over %[ms]s",
                        LazyMessages.this.term,
                        System.currentTimeMillis() - this.start
                    );
                    has = false;
                } else {
                    has = !this.cursor.get().end();
                }
                return has;
            }
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Long next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            synchronized (this.start) {
                this.shifted.set(false);
                final Long number = this.cursor.get().msg().number();
                Logger.debug(
                    this,
                    "#next(): #%d for %[text]s, %[ms]s",
                    number,
                    LazyMessages.this.term,
                    System.currentTimeMillis() - this.start
                );
                return number;
            }
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("#remove()");
        }
    }
}
