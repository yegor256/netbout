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

import java.util.Iterator;

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
     * The cursor.
     */
    private final transient Cursor cursor;

    /**
     * The term.
     */
    private final transient Term term;

    /**
     * Public ctor.
     * @param crs The cursor
     * @param trm The term
     */
    public LazyMessages(final Cursor crs, final Term trm) {
        this.cursor = crs;
        this.term = trm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Long> iterator() {
        return new MessagesIterator(this.cursor);
    }

    /**
     * Iterator, NOT thread safe.
     */
    private final class MessagesIterator implements Iterator<Long> {
        /**
         * Cursor to use.
         */
        private transient Cursor cursor;
        /**
         * Shifted already?
         */
        private transient boolean shifted;
        /**
         * Public ctor.
         * @param crs The cursor
         */
        public MessagesIterator(final Cursor crs) {
            this.cursor = crs.copy();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            if (!this.shifted) {
                this.cursor = this.cursor.shift(LazyMessages.this.term);
                this.shifted = true;
            }
            return this.cursor.valid();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Long next() {
            this.shifted = false;
            return this.cursor.msg().number();
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
