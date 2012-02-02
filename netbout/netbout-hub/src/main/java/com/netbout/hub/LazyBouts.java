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
package com.netbout.hub;

import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import java.util.Iterator;

/**
 * Lazy list of bouts.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class LazyBouts implements Iterable<Bout> {

    /**
     * List of bout numbers.
     */
    private final transient Iterable<Long> bouts;

    /**
     * Where they are.
     */
    private final transient Identity identity;

    /**
     * Public ctor.
     * @param bts The list of bout numbers
     * @param where The bout where they are located
     */
    public LazyBouts(final Iterable<Long> bts, final Identity where) {
        super();
        this.bouts = bts;
        this.identity = where;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Bout> iterator() {
        return new BoutsIterator(this.bouts.iterator());
    }

    /**
     * Iterator.
     */
    private final class BoutsIterator implements Iterator<Bout> {
        /**
         * The iterator to work with.
         */
        private final transient Iterator<Long> iterator;
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
            return this.iterator.hasNext();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Bout next() {
            try {
                return LazyBouts.this.identity.bout(this.iterator.next());
            } catch (com.netbout.spi.BoutNotFoundException ex) {
                throw new IllegalStateException(ex);
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
