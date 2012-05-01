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
import com.netbout.inf.Term;
import com.netbout.inf.TermBuilder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;

/**
 * AND term.
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
        this.terms = args;
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
            final Map<Term, Cursor> cursors = new HashMap<Term, Cursor>();
            for (Term term : this.terms) {
                cursors.put(term, cursor);
            }
            long msg = cursor.msg().number() - 1;
            while (true) {
                slider = this.cycle(cursors, msg);
                if (slider.end() || this.match(cursors.values())) {
                    break;
                }
                msg = slider.msg().number();
            }
        }
        return slider;
    }

    /**
     * Run one cycle through all terms.
     * @param cursors All cursors of all terms
     * @param until Until we reach (or pass) this point
     * @return Result of the cycle run
     */
    private Cursor cycle(final Map<Term, Cursor> cursors, final long until) {
        long anchor = until;
        Cursor slider = new MemCursor(0L, this.imap);
        for (Term term : this.terms) {
            slider = this.slide(cursors.get(term), term, anchor);
            cursors.put(term, slider);
            if (slider.end()) {
                break;
            }
            anchor = slider.msg().number();
        }
        return slider;
    }

    /**
     * Slide down this particular cursor by the term, UNTIL point is
     * reached (or passed).
     * @param cursor The cursor to use
     * @param term The term to use
     * @param until What point to expect
     * @return New cursor, where we stopped (may be the end)
     */
    private Cursor slide(final Cursor cursor, final Term term,
        final long until) {
        Cursor slider = cursor;
        while (true) {
            if (slider.end()) {
                break;
            }
            if (slider.msg().number() <= until) {
                break;
            }
            slider = term.shift(slider);
        }
        return slider;
    }

    /**
     * All cursors point to the same message?
     * @param cursors All cursors of all terms
     * @return TRUE if all of them point to the same message
     */
    private boolean match(final Collection<Cursor> cursors) {
        boolean match = true;
        long msg = Long.MAX_VALUE;
        for (Cursor cursor : cursors) {
            if (cursor.end()) {
                match = false;
                break;
            }
            if (msg == Long.MAX_VALUE) {
                msg = cursor.msg().number();
            }
            if (cursor.msg().number() != msg) {
                match = false;
                break;
            }
        }
        return match;
    }

}
