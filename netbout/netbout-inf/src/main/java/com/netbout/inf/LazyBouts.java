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

import com.netbout.inf.predicates.VariablePred;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Lazy list of bout numbers.
 *
 * <p>It's thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class LazyBouts implements Iterable<Long> {

    /**
     * What is the heap to use.
     */
    private final transient Heap heap;

    /**
     * List of messages.
     */
    private final transient Iterable<Long> messages;

    /**
     * Public ctor.
     * @param where The heap
     * @param msgs The list of them
     */
    public LazyBouts(final Heap where, final Iterable<Long> msgs) {
        this.heap = where;
        this.messages = msgs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Long> iterator() {
        return new BoutsIterator(this.messages.iterator());
    }

    /**
     * Iterator.
     */
    private final class BoutsIterator implements Iterator<Long> {
        /**
         * Found bout numbers already.
         */
        private final transient Set<Long> passed = new HashSet<Long>();
        /**
         * The iterator to work with.
         */
        private final transient Iterator<Long> iterator;
        /**
         * Head of the list, recently loaded.
         */
        private transient Long head;
        /**
         * Current value of {@code head} is vital?
         */
        private transient boolean ready;
        /**
         * Public ctor.
         * @param iter The iterator
         */
        public BoutsIterator(final Iterator<Long> iter) {
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
         * @return The bout number or NULL
         */
        private Long fetch() {
            Long found = null;
            while (this.iterator.hasNext()) {
                final Long msg = this.iterator.next();
                final Long bout = LazyBouts.this.heap.get(msg)
                    .<Long>get(VariablePred.BOUT_NUMBER);
                if (!this.passed.contains(bout)) {
                    found = bout;
                    this.ready = true;
                    this.passed.add(bout);
                    break;
                }
            }
            return found;
        }
    }

}
