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
package com.netbout.inf.functors;

import com.netbout.inf.Atom;
import com.netbout.inf.Cursor;
import com.netbout.inf.Functor;
import com.netbout.inf.Lattice;
import com.netbout.inf.Ray;
import com.netbout.inf.Term;
import com.netbout.inf.atoms.NumberAtom;
import com.netbout.inf.lattice.LatticeBuilder;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Allows only messages from this position and further on.
 *
 * <p>This class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@NamedAs("from")
final class From implements Functor {

    /**
     * {@inheritDoc}
     *
     * <p>There is only one atom accepted and it has to be a {@link NumberAtom},
     * containing the number of position we should start with. Positions are
     * numbers from zero. Thus, if you want to see everything provide ZERO here.
     */
    @Override
    public Term build(final Ray ray, final List<Atom> atoms) {
        return new FromTerm(
            ray,
            NumberAtom.class.cast(atoms.get(0)).value()
        );
    }

    /**
     * The term to use.
     */
    @Term.Volatile
    private static final class FromTerm implements Term {
        /**
         * Ray to work with.
         */
        private final transient Ray ray;
        /**
         * Position required.
         */
        private final transient long from;
        /**
         * How many messages we passed through already.
         */
        private final transient AtomicLong passed = new AtomicLong(-1L);
        /**
         * Most recent message number passed (we keep track of this number
         * in order to avoid duplicate matches).
         */
        private final transient AtomicLong recent =
            new AtomicLong(Long.MAX_VALUE);
        /**
         * Public ctor.
         * @param iray The ray to use
         * @param msg Position to match
         */
        public FromTerm(final Ray iray, final long msg) {
            this.ray = iray;
            this.from = msg;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Term copy() {
            return new From.FromTerm(this.ray, this.from);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Cursor shift(final Cursor cursor) {
            final Term always = this.ray.builder().always();
            Cursor shifted = cursor.shift(always);
            if (!shifted.end()
                && shifted.msg().number() <= this.recent.get()
                && this.passed.incrementAndGet() < this.from) {
                shifted = shifted.shift(always);
                System.out.println("passed: " + this.passed.get());
            }
            if (shifted.end()) {
                this.recent.set(0);
            } else {
                this.recent.set(shifted.msg().number());
            }
            return shifted;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return String.format("(FROM %d)", this.from);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Lattice lattice() {
            return new LatticeBuilder().always().build();
        }
    }

}
