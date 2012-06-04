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
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Cache of terms.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class DefaultCache implements Cache {

    /**
     * Cached terms and their results.
     * @checkstyle LineLength (2 lines)
     */
    private final transient ConcurrentMap<Term, SortedSet<Long>> cached =
        new ConcurrentHashMap<Term, SortedSet<Long>>();

    /**
     * Dependencies.
     * @checkstyle LineLength (2 lines)
     */
    private final transient ConcurrentMap<DependableTerm.Dependency, Set<Term>> deps =
        new ConcurrentHashMap<DependableTerm.Dependency, Set<Term>>();

    /**
     * {@inheritDoc}
     */
    @Override
    public long shift(final Term term, final Cursor cursor) {
        long shifted;
        if (term.getClass().getAnnotation(Cacheable.class) == null) {
            final Cursor cur = term.shift(cursor);
            if (cur.end()) {
                shifted = 0;
            } else {
                shifted = cur.msg().number();
            }
        } else {
            shifted = this.through(term, cursor);
        }
        return shifted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        this.deps.clear();
        this.cached.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear(final String attr) {
        this.clear(new DependableTerm.Dependency(attr));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear(final String attr, final String value) {
        this.clear(new DependableTerm.Dependency(attr, value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        text.append(String.format("%d cached terms\n", this.cached.size()));
        text.append(String.format("%d dependencies\n", this.deps.size()));
        return text.toString();
    }

    /**
     * Clear all terms that match this dep.
     * @param matcher The dependency to use as matcher
     */
    private void clear(final DependableTerm.Dependency matcher) {
        for (DependableTerm.Dependency dep : this.deps.keySet()) {
            if (dep.matches(matcher)) {
                for (Term term : this.deps.get(dep)) {
                    this.cached.remove(term);
                }
                this.deps.remove(dep);
            }
        }
    }

    /**
     * Shift using cache.
     * @param term The term to shift
     * @param cursor Cursor to use
     * @return New cursor
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private long through(final Term term, final Cursor cursor) {
        if (!this.cached.containsKey(term)) {
            this.cached.putIfAbsent(term, this.head(term, cursor));
        }
        if (term instanceof DependableTerm) {
            for (DependableTerm.Dependency dep
                : DependableTerm.class.cast(term).dependencies()) {
                this.deps.putIfAbsent(
                    dep,
                    new CopyOnWriteArraySet<Term>()
                );
                this.deps.get(dep).add(term);
            }
        }
        return this.fetch(term, cursor);
    }

    /**
     * Create head of the row for the given term.
     * @param term The term to shift
     * @param cursor Cursor to use
     * @return The head, until the point of cursor
     */
    private SortedSet<Long> head(final Term term, final Cursor cursor) {
        final SortedSet<Long> head =
            new ConcurrentSkipListSet<Long>(Collections.reverseOrder());
        long msg = Long.MAX_VALUE;
        Cursor shifted = cursor;
        while (!shifted.end() && msg > cursor.msg().number()) {
            shifted = term.shift(shifted);
            if (shifted.end()) {
                msg = 0L;
            } else {
                msg = shifted.msg().number();
            }
            head.add(msg);
        }
        return head;
    }

    /**
     * Fetch next msg number.
     * @param term The term to shift
     * @param cursor Cursor to use
     * @return The number fetched (or ZERO if end of list)
     */
    private long fetch(final Term term, final Cursor cursor) {
        long msg = 0;
        if (!cursor.end() && cursor.msg().number() > 1) {
            final Iterator<Long> iterator = this.cached.get(term)
                .tailSet(cursor.msg().number() - 1).iterator();
            if (iterator.hasNext()) {
                msg = iterator.next();
            } else {
                msg = this.tail(term, cursor);
                this.cached.get(term).add(msg);
            }
        }
        return msg;
    }

    /**
     * Fetch next msg number in the tail.
     * @param term The term to shift
     * @param cursor Cursor to use, after which we need the data
     * @return The number fetched (or ZERO if end of list)
     */
    private long tail(final Term term, final Cursor cursor) {
        final Cursor shifted = term.shift(cursor);
        long msg = 0;
        if (!shifted.end()) {
            msg = shifted.msg().number();
        }
        return msg;
    }

}
