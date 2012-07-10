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

import com.netbout.inf.Attribute;
import com.netbout.inf.Cursor;
import com.netbout.inf.Lattice;
import com.netbout.inf.Term;

/**
 * Matching term.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class MatcherTerm implements Term {

    /**
     * Name of attribute.
     */
    private final transient Attribute attr;

    /**
     * Value to match.
     */
    private final transient String value;

    /**
     * Index map.
     */
    private final transient IndexMap imap;

    /**
     * Hash code, for performance reasons.
     */
    private final transient int hash;

    /**
     * Public ctor.
     * @param map The index map
     * @param atr Attribute
     * @param val Value of it
     */
    public MatcherTerm(final IndexMap map, final Attribute atr,
        final String val) {
        this.imap = map;
        this.attr = atr;
        this.value = val;
        this.hash = this.toString().hashCode();
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
    public boolean equals(final Object term) {
        return term == this || (term instanceof MatcherTerm
            && this.hashCode() == term.hashCode());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("(%s:%s)", this.attr, this.value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Lattice lattice() {
        try {
            return this.imap.index(this.attr).lattice(this.value);
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor shift(final Cursor cursor) {
        try {
            return new MemCursor(
                this.imap.index(this.attr).next(
                    this.value,
                    cursor.msg().number()
                ),
                this.imap
            );
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
