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

import java.util.BitSet;
import java.util.Collection;

/**
 * Lattice of bits.
 *
 * <p>The class is mutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class Lattice {

    /**
     * Total number of bits.
     */
    public static final int BITS = 16384;

    /**
     * Size of one bit, in messages.
     */
    public static final int SIZE = 256;

    /**
     * Fully filled bitset.
     */
    private static final BitSet FULL = Lattice.fullset();

    /**
     * Synchronization mutex.
     */
    private final transient Boolean mutex = Boolean.TRUE;

    /**
     * The bitset.
     */
    private final transient BitSet bitset;

    /**
     * Shifter of cursor.
     */
    public interface Shifter {
        /**
         * Shift cursor to the desired message number.
         * @param cursor The cursor to shift
         * @param msg The message number
         * @return New cursor
         */
        Cursor shift(Cursor cursor, long msg);
    }

    /**
     * Create with all these numbers in the lattice.
     * @param numbers The numbers to add
     */
    public Lattice(final Collection<Long> numbers) {
        this(new BitSet(Lattice.BITS));
        for (Long num : numbers) {
            this.bitset.set(this.bit(num));
        }
    }

    /**
     * Create with one number in the lattice.
     * @param number The number to add
     */
    public Lattice(final long number) {
        this(new BitSet(Lattice.BITS));
        this.bitset.set(this.bit(number));
    }

    /**
     * Create an new lattice.
     * @param bset The bitset
     */
    private Lattice(final BitSet bset) {
        this.bitset = bset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.bitset.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.bitset.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object lattice) {
        return this == lattice || (lattice instanceof Lattice
            && this.hashCode() == lattice.hashCode());
    }

    /**
     * Always TRUE.
     * @return The lattice
     */
    public static Lattice always() {
        return new Lattice(Lattice.FULL);
    }

    /**
     * Always FALSE.
     * @return The lattice
     */
    public static Lattice never() {
        return new Lattice(new BitSet(Lattice.BITS));
    }

    /**
     * Join them all together (AND).
     * @param terms The terms to merge
     */
    public void and(final Collection<Term> terms) {
        for (Term term : terms) {
            this.and(term.lattice());
        }
    }

    /**
     * Join them all together (OR).
     * @param terms The terms to merge
     * @checkstyle MethodName (3 lines)
     */
    @SuppressWarnings("PMD.ShortMethodName")
    public void or(final Collection<Term> terms) {
        for (Term term : terms) {
            this.or(term.lattice());
        }
    }

    /**
     * AND this lattice with a new one.
     * @param lattice The lattice to apply
     */
    public void and(final Lattice lattice) {
        synchronized (this.mutex) {
            this.bitset.and(lattice.bitset);
        }
    }

    /**
     * OR this lattice with a new one.
     * @param lattice The lattice to apply
     * @checkstyle MethodName (3 lines)
     */
    @SuppressWarnings("PMD.ShortMethodName")
    public void or(final Lattice lattice) {
        synchronized (this.mutex) {
            this.bitset.or(lattice.bitset);
        }
    }

    /**
     * Reverse this lattice.
     */
    public void reverse() {
        this.bitset.xor(Lattice.FULL);
    }

    /**
     * Correct this cursor and return a new one, which is more likely to
     * match one of the numbers in the lattice.
     * @param cursor The cursor to start from
     * @param shifter The shifter to use
     * @return The new cursor
     */
    public Cursor correct(final Cursor cursor, final Lattice.Shifter shifter) {
        Cursor corrected;
        if (cursor.end()) {
            corrected = cursor;
        } else {
            final int bit = this.bit(cursor.msg().number());
            final int next = this.bitset.nextSetBit(bit);
            if (next > bit) {
                corrected = shifter.shift(cursor, this.msg(next));
            } else {
                corrected = cursor;
            }
        }
        return corrected;
    }

    /**
     * Get the number of the bit for this number.
     * @param number The number
     * @return The bit
     */
    private int bit(final long number) {
        return Lattice.BITS - (int) number / Lattice.SIZE;
    }

    /**
     * Get the number of the message from the bit.
     * @param bit The bit
     * @return The message number
     */
    private long msg(final int bit) {
        return (Lattice.BITS - bit + 1) * Lattice.SIZE - 1;
    }

    /**
     * Create and return a set full of ONE-s.
     * @return The set
     */
    private static BitSet fullset() {
        final BitSet bset = new BitSet(Lattice.BITS);
        bset.set(0, Lattice.BITS, true);
        return bset;
    }

}
