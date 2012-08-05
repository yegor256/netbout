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
package com.netbout.inf.lattice;

import com.jcabi.log.Logger;
import com.netbout.inf.Cursor;
import com.netbout.inf.Lattice;
import java.util.BitSet;

/**
 * Lattice on top of bitset.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
final class BitsetLattice implements Lattice {

    /**
     * Total maximum number of bits in the lattice (16384).
     */
    public static final int BITS = 16384;

    /**
     * Window size of every bit, in messages.
     */
    public static final int SIZE = 64;

    /**
     * The main bitset (also accessible from {@link LatticeBuilder}).
     * @checkstyle VisibilityModifier (3 lines)
     */
    @SuppressWarnings("PMD.AvoidProtectedFieldInFinalClass")
    protected final transient BitSet main;

    /**
     * The reverse bitset (also accessible from {@link LatticeBuilder}).
     * @checkstyle VisibilityModifier (3 lines)
     */
    @SuppressWarnings("PMD.AvoidProtectedFieldInFinalClass")
    protected final transient BitSet reverse;

    /**
     * Public ctor.
     * @param bset Main bitset
     * @param rev Reverse bitset
     */
    public BitsetLattice(final BitSet bset, final BitSet rev) {
        this.main = bset;
        this.reverse = rev;
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
        return this.toString().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object lattice) {
        return this == lattice || (lattice instanceof BitsetLattice
            && this.hashCode() == lattice.hashCode());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor correct(final Cursor cursor, final Lattice.Shifter shifter) {
        long number = cursor.msg().number();
        if (number == Long.MAX_VALUE) {
            number = BitsetLattice.BITS * BitsetLattice.SIZE;
        }
        final int bit = BitsetLattice.bit(Math.max(number - 1, 1L));
        final int next = this.main.nextSetBit(bit);
        Cursor corrected;
        if (next == -1) {
            corrected = shifter.shift(cursor, 0L);
        } else if (next > bit) {
            final long msg = BitsetLattice.msg(next) + 1;
            corrected = shifter.shift(cursor, msg);
            Logger.debug(
                this,
                "#correct(#%d, %s): moved to #%d (bit=%d, next=%d)",
                cursor.msg().number(),
                shifter,
                msg,
                bit,
                next
            );
        } else {
            corrected = cursor;
        }
        return corrected;
    }

    /**
     * Get the number of the bit for this number.
     * @param number The number
     * @return The bit
     */
    @SuppressWarnings("PMD.DefaultPackage")
    static int bit(final long number) {
        if (number > BitsetLattice.BITS * BitsetLattice.SIZE) {
            throw new LatticeException(
                String.format("message #%d is out of range", number)
            );
        }
        if (number <= 0) {
            throw new LatticeException(
                String.format("message #%d is negative or zero", number)
            );
        }
        return BitsetLattice.BITS - (int) (number - 1) / BitsetLattice.SIZE - 1;
    }

    /**
     * Get the number of the message from the bit.
     * @param bit The bit
     * @return The message number
     * @see DefaultIndex#emptyBit(String,long)
     */
    @SuppressWarnings("PMD.DefaultPackage")
    static long msg(final int bit) {
        if (bit >= BitsetLattice.BITS || bit < 0) {
            throw new LatticeException(
                String.format("bit #%d is out of range", bit)
            );
        }
        return (BitsetLattice.BITS - bit) * BitsetLattice.SIZE;
    }

}