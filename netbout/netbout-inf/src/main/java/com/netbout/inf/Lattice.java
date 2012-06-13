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
     * Full coverage.
     */
    public static final Lattice ALWAYS = new Lattice(
        Arrays.asList(new Long[] {0L, Long.MAX_VALUE})
    );

    /**
     * No coverage at all.
     */
    public static final Lattice NEVER = new Lattice(0);

    /**
     * Create with all these numbers in the lattice.
     * @param numbers The numbers to add
     */
    public Lattice(final Collection<Long> numbers) {
        // todo
    }

    /**
     * Create with one number in the lattice.
     * @param number The number to add
     */
    public Lattice(final long number) {
        // todo
    }

    /**
     * Join them all together (AND).
     * @param terms The terms to merge
     * @return New lattice
     */
    public static Lattice and(final Collection<Term> terms) {
        return Lattice.ALWAYS;
    }

    /**
     * Join them all together (OR).
     * @param terms The terms to merge
     * @return New lattice
     * @checkstyle MethodName (3 lines)
     */
    public static Lattice or(final Collection<Term> terms) {
        return Lattice.ALWAYS;
    }

    /**
     * AND this lattice with a new one.
     * @param lattice The lattice to apply
     */
    public void and(final Lattice lattice) {
    }

    /**
     * Reverse this lattice.
     * @return New lattice, reversed
     */
    public Lattice reverse() {
        return Lattice.ALWAYS;
    }

    /**
     * Correct this cursor and return a new one, which is more likely to
     * match one of the numbers in the lattice.
     * @param cursor The cursor to start from
     * @return The new cursor
     */
    public Cursor correct(final Cursor cursor) {
        return cursor;
    }

}
