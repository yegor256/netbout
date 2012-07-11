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
import java.util.concurrent.atomic.AtomicLong;

/**
 * One-way valve.
 *
 * <p>Allows cursors only in one direction, from bigger to smaller.
 *
 * <p>The class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class Valve implements Term {

    /**
     * Original term.
     */
    private final transient Term origin;

    /**
     * The maximum number we are ready to accept.
     */
    private transient long max = Long.MAX_VALUE;

    /**
     * Public ctor.
     * @param term Original term
     */
    public Valve(final Term term) {
        this.origin = term;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Term copy() {
        return new Valve(this.origin.copy());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("x:%s", this.origin.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.origin.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object term) {
        return this == term || (term instanceof Term &&
            this.hashCode() == term.hashCode());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor shift(Cursor cursor) {
        final long number = cursor.msg().number();
        if (this.max == 0) {
            throw new IllegalArgumentException(
                String.format(
                    "%s can't be used any more",
                    this
                )
            );
        }
        if (number > this.max) {
            throw new IllegalArgumentException(
                String.format(
                    "%s can't go reverse to %s (max=%d)",
                    this,
                    cursor,
                    this.max
                )
            );
        }
        if (cursor.end()) {
            throw new IllegalArgumentException("end of cursor");
        }
        final Cursor shifted = this.origin.shift(cursor);
        if (shifted.end()) {
            this.max = 0;
        } else {
            this.max = shifted.msg().number();
        }
        return shifted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Lattice lattice() {
        return this.origin.lattice();
    }

}
