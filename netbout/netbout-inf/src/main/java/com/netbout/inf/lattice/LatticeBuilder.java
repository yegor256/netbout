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
 * this code accidentally and without intent to use it, please report this
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
package com.netbout.inf.lattice;

import com.netbout.inf.Lattice;
import com.netbout.inf.Term;
import java.util.BitSet;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Builder of Lattice.
 *
 * <p>The class is mutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class LatticeBuilder {

    /**
     * Was it started with {@link #never()} or {@link #always()}.
     */
    private final transient AtomicBoolean started = new AtomicBoolean(false);

    /**
     * The main bitset.
     */
    private transient BitSet main = new BitSet(BitsetLattice.BITS);

    /**
     * The reverse bitset, where ONE means that there is at least one ZEROs
     * in this bit at the main bitset.
     */
    private transient BitSet reverse = new BitSet(BitsetLattice.BITS);

    /**
     * Build the lattice.
     * @return The lattice built
     */
    public Lattice build() {
        return new BitsetLattice(
            BitSet.class.cast(this.main.clone()),
            BitSet.class.cast(this.reverse.clone())
        );
    }

    /**
     * Fill lattice with this reverse-ordered set of numbers.
     * @param feeder The numbers to use
     * @return This object
     */
    public LatticeBuilder fill(final Feeder feeder) {
        synchronized (this.started) {
            this.main.clear(0, BitsetLattice.BITS);
            this.reverse.clear(0, BitsetLattice.BITS);
            long previous = Long.MAX_VALUE;
            long delta = 0;
            while (true) {
                final long num = feeder.next();
                if (num == 0) {
                    break;
                }
                if (num == previous) {
                    throw new LatticeException(
                        "duplicate numbers not allowed"
                    );
                }
                if (num > previous) {
                    throw new LatticeException(
                        "numbers should be reverse-ordered"
                    );
                }
                final int bit = BitsetLattice.bit(num);
                this.main.set(bit, true);
                delta = previous - num;
                if (delta > 1) {
                    LatticeBuilder.range(this.reverse, previous, num);
                }
                previous = num;
            }
            if (previous > 1L) {
                LatticeBuilder.range(this.reverse, previous, 0L);
            }
            this.started.set(true);
        }
        return this;
    }

    /**
     * Copy existing lattice to the builder (all data in the builder are
     * destroyed).
     * @param lattice The lattice to copy
     * @return This object
     */
    public LatticeBuilder copy(final Lattice lattice) {
        synchronized (this.started) {
            this.main = BitSet.class.cast(
                BitsetLattice.class.cast(lattice).main.clone()
            );
            this.reverse = BitSet.class.cast(
                BitsetLattice.class.cast(lattice).reverse.clone()
            );
        }
        this.started.set(true);
        return this;
    }

    /**
     * Set lattice to always true.
     * @return This object
     */
    public LatticeBuilder always() {
        synchronized (this.started) {
            this.main.set(0, BitsetLattice.BITS);
            this.reverse.clear(0, BitsetLattice.BITS);
        }
        this.started.set(true);
        return this;
    }

    /**
     * Set lattice to never true.
     * @return This object
     */
    public LatticeBuilder never() {
        synchronized (this.started) {
            this.main.clear(0, BitsetLattice.BITS);
            this.reverse.set(0, BitsetLattice.BITS);
        }
        this.started.set(true);
        return this;
    }

    /**
     * AND all lattices from the provided terms.
     * @param terms Terms to get lattices from
     * @return This object
     */
    public LatticeBuilder and(final Collection<Term> terms) {
        if (!this.started.get()) {
            throw new LatticeException(
                "can't call #and(), start with always(), fill(), or never()"
            );
        }
        synchronized (this.started) {
            for (Term term : terms) {
                final BitsetLattice lattice =
                    BitsetLattice.class.cast(term.lattice());
                this.main.and(lattice.main);
                this.reverse.or(lattice.reverse);
            }
        }
        return this;
    }

    /**
     * OR all lattices from the provided terms.
     * @param terms Terms to get lattices from
     * @return This object
     * @checkstyle MethodName (4 lines)
     */
    @SuppressWarnings("PMD.ShortMethodName")
    public LatticeBuilder or(final Collection<Term> terms) {
        if (!this.started.get()) {
            throw new LatticeException(
                "can't call #or(), start with always(), fill(), or never()"
            );
        }
        synchronized (this.started) {
            for (Term term : terms) {
                final BitsetLattice lattice =
                    BitsetLattice.class.cast(term.lattice());
                this.main.or(lattice.main);
                this.reverse.and(lattice.reverse);
            }
        }
        return this;
    }

    /**
     * Revert the lattice.
     * @return This object
     */
    public LatticeBuilder revert() {
        if (!this.started.get()) {
            throw new LatticeException(
                "can't call #revert(), start with always(), fill(), or never()"
            );
        }
        synchronized (this.started) {
            final BitSet temp = this.main;
            this.main = this.reverse;
            this.reverse = temp;
        }
        return this;
    }

    /**
     * Update status for this particular message, according to other msgs.
     * @param number The number of message
     * @param range Where it is happening
     * @return This object
     */
    public LatticeBuilder update(final long number, final Range range) {
        if (!this.started.get()) {
            throw new LatticeException(
                "can't call #set(), start with always(), fill(), or never()"
            );
        }
        synchronized (this.started) {
            final int bit = BitsetLattice.bit(number);
            long right;
            if (bit < BitsetLattice.BITS - 1) {
                right = BitsetLattice.msg(bit + 1);
            } else {
                right = 0L;
            }
            final int window = range.window(BitsetLattice.msg(bit), right);
            this.main.set(bit, window > 0);
            this.reverse.set(bit, window < BitsetLattice.SIZE);
        }
        return this;
    }

    /**
     * Set reverse bits for the range, where we assume that all numbers between
     * provided "left" and "right" are missed, and ONEs should be set for
     * them in the provided bitset.
     * @param bitset The bitset to use
     * @param left FROM message number (bigger than TO, and bigger than 1L)
     * @param right TO message number
     */
    private static void range(final BitSet bitset, final long left,
        final long right) {
        int lbit;
        if (left == Long.MAX_VALUE) {
            lbit = 0;
        } else {
            lbit = BitsetLattice.bit(left - 1);
        }
        final int rbit = BitsetLattice.bit(right + 1);
        bitset.set(lbit, rbit + 1);
    }

}
