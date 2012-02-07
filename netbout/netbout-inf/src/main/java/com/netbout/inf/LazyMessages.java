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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Lazy list of message numbers.
 *
 * <p>It's thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class LazyMessages implements Iterable<Long> {

    /**
     * List of messages.
     */
    private final transient Iterable<Msg> messages;

    /**
     * The predicate.
     */
    private final transient Predicate predicate;

    /**
     * Public ctor.
     * @param msgs The list of messages
     * @param pred The predicate
     */
    public LazyMessages(final Iterable<Msg> msgs, final Predicate pred) {
        this.messages = msgs;
        this.predicate = pred;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Long> iterator() {
        return new MessagesIterator(this.messages.iterator());
    }

    /**
     * Iterator.
     */
    private final class MessagesIterator implements Iterator<Long> {
        /**
         * The iterator to work with.
         */
        private final transient Iterator<Msg> iterator;
        /**
         * Head of the list, recently loaded (or NULL if it's the end).
         */
        private transient Long head;
        /**
         * Position in the list.
         */
        private transient int position;
        /**
         * Current value of {@code head} is vital?
         */
        private transient boolean ready;
        /**
         * Public ctor.
         * @param iter The iterator
         */
        public MessagesIterator(final Iterator<Msg> iter) {
            this.iterator = iter;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            synchronized (this) {
                if (!this.ready) {
                    this.head = this.fetch();
                }
                return this.ready;
            }
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Long next() {
            synchronized (this) {
                if (!this.ready) {
                    this.head = this.fetch();
                }
                if (!this.ready) {
                    throw new NoSuchElementException();
                }
                this.ready = false;
                return this.head;
            }
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("#remove()");
        }
        /**
         * Fetch the next element or return NULL.
         * @return The message number or NULL
         */
        private Long fetch() {
            Long found = null;
            while (this.iterator.hasNext()) {
                final Msg msg = this.iterator.next();
                if (msg != null && (Boolean) LazyMessages.this.predicate
                    .evaluate(msg, this.position)) {
                    found = msg.number();
                    this.ready = true;
                    this.position += 1;
                    break;
                }
            }
            return found;
        }
    }

}
