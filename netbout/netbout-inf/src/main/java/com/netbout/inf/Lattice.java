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

import java.util.Collection;
import java.util.SortedSet;

/**
 * Lattice of bits.
 *
 * <p>Implementation has to be mutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public interface Lattice {

    /**
     * Shifter of cursor.
     */
    interface Shifter {
        /**
         * Shift cursor to the desired message number.
         * @param cursor The cursor to shift
         * @param msg The message number
         * @return New cursor
         */
        Cursor shift(Cursor cursor, long msg);
    }

    /**
     * Fill lattice with these numbers (normally the lattice has to be empty
     * before this operation).
     * @param numbers The numbers to add
     */
    void fill(SortedSet<Long> numbers);

    /**
     * Create a copy of this object.
     * @return The same lattice, but as a new object
     */
    Lattice copy();

    /**
     * Always TRUE (set all bits to TRUE).
     */
    void always();

    /**
     * Join them all together (AND).
     * @param terms The terms to merge
     */
    void and(Collection<Term> terms);

    /**
     * Join them all together (OR).
     * @param terms The terms to merge
     * @checkstyle MethodName (3 lines)
     */
    @SuppressWarnings("PMD.ShortMethodName")
    void or(Collection<Term> terms);

    /**
     * Set main and reverse bit for this message.
     * @param number The number of message
     * @param bit Main bit to set to
     * @param rev Value of reverse bit to set to
     */
    void set(long number, boolean bit, boolean rev);

    /**
     * Revert it.
     */
    void revert();

    /**
     * Correct this cursor and return a new one, which is more likely to
     * match one of the numbers in the lattice.
     * @param cursor The cursor to start from
     * @param shifter The shifter to use
     * @return The new cursor
     */
    Cursor correct(Cursor cursor, Lattice.Shifter shifter);

    /**
     * Do we have an empty bit for this message.
     *
     * <p>The method checks all message around the provided one inside the
     * the provided set and returns TRUE only if nothing is found near by. The
     * distance boundaries around the message are defined by the size of
     * the window of this lattice.
     *
     * @param numbers The numbers to work with
     * @param msg The message number
     * @return TRUE if we have no messages around this one
     */
    boolean emptyBit(SortedSet<Long> numbers, long msg);

}
