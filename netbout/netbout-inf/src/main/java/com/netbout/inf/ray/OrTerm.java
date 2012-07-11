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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * OR term.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class OrTerm implements Term {

    /**
     * Terms (also visible from {@link AndTerm}).
     * @checkstyle VisibilityModifier (3 lines)
     */
    @SuppressWarnings("PMD.AvoidProtectedFieldInFinalClass")
    protected final transient Set<Term> terms = new LinkedHashSet<Term>();

    /**
     * Hash code, for performance reasons.
     */
    private final transient int hash;

    /**
     * Index map.
     */
    private final transient IndexMap imap;

    /**
     * Cached cursors from every term.
     */
    private final transient ConcurrentMap<Term, Cursor> cache =
        new ConcurrentHashMap<Term, Cursor>();

    /**
     * Public ctor.
     * @param map The index map
     * @param args Arguments (terms)
     */
    public OrTerm(final IndexMap map, final Collection<Term> args) {
        this.imap = map;
        for (Term arg : args) {
            if (arg instanceof OrTerm) {
                this.terms.addAll(OrTerm.class.cast(arg).terms);
            } else if (arg instanceof AndTerm
                && AndTerm.class.cast(arg).terms.size() == 1) {
                this.terms.addAll(AndTerm.class.cast(arg).terms);
            } else {
                this.terms.add(arg);
            }
        }
        if (this.terms.isEmpty()) {
            this.terms.add(new AlwaysTerm(this.imap));
        }
        this.hash = this.toString().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Term copy() {
        final Collection<Term> copies = new ArrayList<Term>(this.terms.size());
        for (Term term : this.terms) {
            copies.add(term.copy());
        }
        return new OrTerm(this.imap, copies);
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
        return term == this || (term instanceof OrTerm
            && this.hashCode() == term.hashCode());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        text.append("(OR");
        for (Term term : this.terms) {
            text.append(' ').append(term);
        }
        return text.append(')').toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Lattice lattice() {
        return new LatticeBuilder().never().or(this.terms).build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor shift(final Cursor cursor) {
        Cursor found = null;
        for (Term term : this.terms) {
            if (!this.cache.containsKey(term)) {
                this.cache.put(term, cursor.shift(term));
            }
            if (this.cache.get(term).compareTo(cursor) >= 0) {
                this.cache.put(term, cursor.shift(term));
            }
            final Cursor candidate = this.cache.get(term);
            if (found == null || candidate.compareTo(found) > 0) {
                found = candidate;
            }
        }
        if (found == null) {
            found = new MemCursor(0L, this.imap);
        }
        return found;
    }

}
