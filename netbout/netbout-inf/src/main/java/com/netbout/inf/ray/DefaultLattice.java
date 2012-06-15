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
package com.netbout.inf.ray;

import com.jcabi.log.Logger;
import com.netbout.inf.Cursor;
import com.netbout.inf.Lattice;
import com.netbout.inf.Term;
import java.util.BitSet;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Lattice of bits.
 *
 * <p>The class is mutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
final class DefaultLattice implements Lattice {

    /**
     * The main bitset.
     */
    private transient BitSet main = new BitSet(Lattice.BITS);

    /**
     * The reverse bitset.
     */
    private transient BitSet reverse = new BitSet(Lattice.BITS);

    /**
     * Create NEVER lattice.
     */
    public DefaultLattice() {
        this(new TreeSet<Long>());
    }

    /**
     * Create with all these numbers in the lattice.
     * @param numbers The numbers to add
     */
    public DefaultLattice(final SortedSet<Long> numbers) {
        long previous = Long.MAX_VALUE;
        for (Long num : numbers) {
            if (previous - num < Lattice.SIZE) {
                continue;
            }
            previous = num;
            this.main.set(DefaultLattice.bit(num));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.main.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.main.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object lattice) {
        return this == lattice || (lattice instanceof DefaultLattice
            && this.hashCode() == lattice.hashCode());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void always() {
        synchronized (this.main) {
            this.main.set(0, Lattice.BITS, true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void and(final Collection<Term> terms) {
        synchronized (this.main) {
            for (Term term : terms) {
                this.main.and(
                    DefaultLattice.class.cast(term.lattice()).main
                );
                this.reverse.or(
                    DefaultLattice.class.cast(term.lattice()).reverse
                );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.ShortMethodName")
    public void or(final Collection<Term> terms) {
        synchronized (this.main) {
            for (Term term : terms) {
                this.main.or(
                    DefaultLattice.class.cast(term.lattice()).main
                );
                this.reverse.and(
                    DefaultLattice.class.cast(term.lattice()).reverse
                );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void revert() {
        synchronized (this.main) {
            final BitSet temp = this.main;
            this.main = this.reverse;
            this.reverse = temp;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor correct(final Cursor cursor, final Lattice.Shifter shifter) {
        Cursor corrected;
        if (cursor.end()) {
            corrected = cursor;
        } else {
            final int bit = DefaultLattice.bit(cursor.msg().number());
            final int next = this.main.nextSetBit(bit);
            if (next != -1 && next > bit) {
                corrected = shifter.shift(cursor, DefaultLattice.msg(next));
                Logger.debug(
                    this,
                    "#correct(%s, ..): moved to %s",
                    cursor,
                    corrected
                );
            } else {
                corrected = cursor;
            }
        }
        return corrected;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(final long number, final boolean bit, final boolean rev) {
        synchronized (this.main) {
            final int num = DefaultLattice.bit(number);
            if (bit) {
                this.main.set(num);
            }
            if (rev) {
                this.reverse.set(num);
            }
        }
    }

    /**
     * Get the number of the bit for this number.
     * @param number The number
     * @return The bit
     * @see DefaultIndex#emptyBit(String,long)
     */
    protected static int bit(final long number) {
        return Lattice.BITS - (int) number / Lattice.SIZE;
    }

    /**
     * Get the number of the message from the bit.
     * @param bit The bit
     * @return The message number
     * @see DefaultIndex#emptyBit(String,long)
     */
    protected static long msg(final int bit) {
        return (Lattice.BITS - bit + 1) * Lattice.SIZE - 1;
    }

}
