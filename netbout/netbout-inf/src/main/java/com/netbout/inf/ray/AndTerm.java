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

import com.jcabi.log.Logger;
import com.netbout.inf.Cursor;
import com.netbout.inf.Term;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * AND term.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class AndTerm implements Term {

    /**
     * Index map.
     */
    private final transient IndexMap imap;

    /**
     * Terms.
     */
    private final transient Collection<Term> terms;

    /**
     * Public ctor.
     * @param map The index map
     * @param args Arguments (terms)
     */
    public AndTerm(final IndexMap map, final Collection<Term> args) {
        this.imap = map;
        this.terms = new ArrayList<Term>(args);
        if (this.terms.isEmpty()) {
            this.terms.add(new AlwaysTerm(this.imap));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        text.append("(AND");
        for (Term term : this.terms) {
            text.append(' ').append(term);
        }
        text.append(')');
        return text.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor shift(final Cursor cursor) {
        Cursor slider;
        if (cursor.end()) {
            slider = cursor;
        } else {
            final ConcurrentMap<Term, Cursor> cache =
                new ConcurrentHashMap<Term, Cursor>();
            slider = this.move(this.terms.iterator().next(), cursor, cache);
            if (!slider.end()) {
                slider = this.slide(slider, cache);
            }
        }
        Logger.debug(this, "#shift(%s): to %s", cursor, slider);
        return slider;
    }

    /**
     * Slide it down to the first match.
     * @param cursor First expected point to reach
     * @param cache Cached positions of every term
     * @return Matched position (or END)
     */
    private Cursor slide(final Cursor cursor,
        final ConcurrentMap<Term, Cursor> cache) {
        Cursor slider = cursor;
        boolean match;
        do {
            match = true;
            final Cursor expected = slider;
            for (Term term : this.terms) {
                slider = this.move(term, this.above(slider), cache);
                if (!expected.equals(slider)) {
                    match = false;
                    break;
                }
            }
        } while (!match && !slider.end());
        return slider;
    }

    /**
     * One step above.
     * @param cursor The cursor
     * @return Cursor with one step higher position
     */
    private Cursor above(final Cursor cursor) {
        if (cursor.msg().number() == Long.MAX_VALUE) {
            throw new IllegalArgumentException("can't use above()");
        }
        return new MemCursor(cursor.msg().number() + 1, this.imap);
    }

    /**
     * Move term one step next.
     * @param term The term to use for movement
     * @param from Where to start
     * @param cache Cached positions of every term
     * @return Matched position (or END)
     */
    private Cursor move(final Term term, final Cursor from,
        final ConcurrentMap<Term, Cursor> cache) {
        if (!cache.containsKey(term)
            || cache.get(term).compareTo(from) >= 0
            || term.getClass().getAnnotation(Term.Volatile.class) != null) {
            cache.put(term, term.shift(from));
        }
        return cache.get(term);
    }

}
