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

import com.netbout.inf.Cursor;
import com.netbout.inf.Lattice;
import com.netbout.inf.Term;
import com.netbout.inf.lattice.LatticeBuilder;
import java.util.Arrays;
import java.util.TreeSet;

/**
 * Fast-jump term, that is treated by MemCursor in a special way, see
 * {@code MemCursor#shift(Cursor)}.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class JumpTerm implements Term {

    /**
     * Number of message to pick.
     */
    private final transient long number;

    /**
     * Public ctor.
     * @param num The number to jump to
     */
    public JumpTerm(final long num) {
        this.number = num;
    }

    /**
     * Get message number.
     * @return The number
     */
    public long msg() {
        return this.number;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Term copy() {
        throw new UnsupportedOperationException(
            "Method #copy() should never be called"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        throw new UnsupportedOperationException(
            "Method #hashCode() should never be called"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object term) {
        throw new UnsupportedOperationException(
            "Method #equals() should never be called"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("(JUMP %d)", this.number);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Lattice lattice() {
        throw new UnsupportedOperationException(
            "Method #lattice() should never be called"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor shift(final Cursor cursor) {
        throw new UnsupportedOperationException();
    }

}
