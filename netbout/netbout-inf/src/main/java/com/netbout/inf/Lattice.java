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

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;

/**
 * Lattice of bits.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
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
     * Full coverage.
     */
    public static final Lattice ALWAYS = new Lattice(Lattice.fullset());

    /**
     * No coverage at all.
     */
    public static final Lattice NEVER = new Lattice(new BitSet());

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
     * Create an new lattice.
     * @param bset The bitset
     */
    private Lattice(final BitSet bset) {
        this.bitset = bset;
    }

    /**
     * Create with all these numbers in the lattice.
     * @param numbers The numbers to add
     */
    public Lattice(final Collection<Long> numbers) {
        this(new BitSet());
        for (Long num : numbers) {
            this.bitset.set(this.bit(num));
        }
    }

    /**
     * Create with one number in the lattice.
     * @param number The number to add
     */
    public Lattice(final long number) {
        this(new BitSet());
        this.bitset.set(this.bit(number));
    }

    /**
     * Join them all together (AND).
     * @param terms The terms to merge
     * @return New lattice
     */
    public static Lattice and(final Collection<Term> terms) {
        final Lattice lattice = Lattice.ALWAYS;
        for (Term term : terms) {
            lattice.and(term.lattice());
        }
        return lattice;
    }

    /**
     * Join them all together (OR).
     * @param terms The terms to merge
     * @return New lattice
     * @checkstyle MethodName (3 lines)
     */
    public static Lattice or(final Collection<Term> terms) {
        final Lattice lattice = Lattice.NEVER;
        for (Term term : terms) {
            lattice.or(term.lattice());
        }
        return lattice;
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
     */
    public void or(final Lattice lattice) {
        synchronized (this.mutex) {
            this.bitset.or(lattice.bitset);
        }
    }

    /**
     * Reverse this lattice.
     * @return New lattice, reversed
     */
    public Lattice reverse() {
        final BitSet bset = BitSet.class.cast(this.bitset.clone());
        bset.xor(Lattice.ALWAYS.bitset);
        return new Lattice(bset);
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
        return (Lattice.BITS - bit) * Lattice.SIZE;
    }

    /**
     * Create and return a set full of ONE-s.
     * @return The set
     */
    private static BitSet fullset() {
        final BitSet bset = new BitSet();
        bset.set(0, Lattice.BITS, true);
        return bset;
    }

}
