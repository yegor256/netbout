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
import java.util.concurrent.atomic.AtomicReference;

/**
 * NOT term.
 *
 * <p>The class is mutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class NotTerm implements Term {

    /**
     * Hash code, for performance reasons.
     */
    private final transient int hash;

    /**
     * Index map.
     */
    private final transient IndexMap imap;

    /**
     * Term to negate.
     */
    private final transient Term term;

    /**
     * Most recent position of the matcher.
     */
    private final transient AtomicReference<Cursor> matcher =
        new AtomicReference<Cursor>();

    /**
     * Shifter for lattice.
     */
    private final transient Lattice.Shifter shifter = new Lattice.Shifter() {
        @Override
        public Cursor shift(final Cursor crsr, final long msg) {
            if (msg >= crsr.msg().number()) {
                throw new IllegalArgumentException("shift back is prohibited");
            }
            return crsr.shift(new PickerTerm(NotTerm.this.imap, msg));
        }
        @Override
        public String toString() {
            return NotTerm.this.toString();
        }
    };

    /**
     * Public ctor.
     * @param map The index map
     * @param trm The term
     */
    public NotTerm(final IndexMap map, final Term trm) {
        this.imap = map;
        this.term = trm;
        this.hash = this.toString().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Term copy() {
        return new NotTerm(this.imap, this.term.copy());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.hash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object trm) {
        return trm == this || (trm instanceof NotTerm
            && this.hashCode() == trm.hashCode());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("(NOT %s)", this.term);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Lattice lattice() {
        return new LatticeBuilder()
            .copy(this.term.lattice())
            .revert()
            .build();
    }

    /**
     * {@inheritDoc}
     *
     * <p>There are two cursors that we shift down at the same time. The first
     * one ({@code always}) always shifts one message down. The second one
     * ({@code this.matcher}) goes independently, according to the incapsulated
     * term. The matcher is kept in the class, in order to avoid duplicate
     * shifting of the incapsulated term.
     */
    @Override
    public Cursor shift(final Cursor cursor) {
        final Cursor corrected = this.lattice().correct(cursor, this.shifter);
        Cursor always = corrected;
        if (!always.end()) {
            final Term aterm = new AlwaysTerm(this.imap);
            if (this.matcher.get() == null
                || cursor.compareTo(this.matcher.get()) < 0) {
                this.matcher.set(corrected.shift(this.term));
            }
            while (true) {
                always = always.shift(aterm);
                if (always.end() || this.matcher.get().end()) {
                    break;
                }
                if (always.compareTo(this.matcher.get()) > 0) {
                    break;
                }
                this.matcher.set(this.matcher.get().shift(this.term));
            }
        }
        return always;
    }

}
