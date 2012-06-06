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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Cache of terms.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
final class DefaultCache implements Cache {

    /**
     * Cached terms and their cached results (called Numbers).
     */
    private final transient ConcurrentMap<Term, DefaultCache.Numbers> cached =
        new ConcurrentHashMap<Term, DefaultCache.Numbers>();

    /**
     * Term has to be cached?
     * @param term The term to check
     * @return Is it cacheable or not?
     */
    public static boolean isCacheable(final Term term) {
        return term.getClass().getAnnotation(Term.Volatile.class) == null
            && term.getClass().getAnnotation(Term.Uncacheable.class) == null
            && (!(term instanceof Cacheable)
                || Cacheable.class.cast(term).cacheThis());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long shift(final Term term, final Cursor cursor) {
        long shifted;
        if (DefaultCache.isCacheable(term)
            && term.getClass().getAnnotation(Term.Cheap.class) == null) {
            shifted = this.through(term, cursor);
        } else {
            final Cursor cur = term.shift(cursor);
            if (cur.end()) {
                shifted = 0;
            } else {
                shifted = cur.msg().number();
            }
        }
        return shifted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
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
        text.append(String.format("%d cached term(s):\n", this.cached.size()));
        int total = 0;
        for (Term term : this.cached.keySet()) {
            text.append(
                Logger.format("%[text]s: %s\n", term, this.cached.get(term))
            );
            // @checkstyle MagicNumber (1 line)
            if (++total > 20) {
                text.append("others skipped...\n");
                break;
            }
        }
        return text.toString();
    }

    /**
     * Numbers collected for a term.
     */
    private interface Numbers {
        /**
         * Fetch next msg number.
         * @param term The term to shift
         * @param cursor Cursor to use
         * @return The number fetched (or ZERO if end of list)
         */
        long fetch(Term term, Cursor cursor);
        /**
         * This numbers depend on the given dependency?
         * @param dep The dependency
         * @return Yes or no
         */
        boolean dependsOn(DependableTerm.Dependency dep);
    }

    /**
     * Readl term numbers, implementation.
     */
    private static final class RealNumbers implements DefaultCache.Numbers {
        /**
         * Ordered set of cached msg numbers.
         */
        private transient SortedSet<Long> msgs;
        /**
         * Dependencies.
         */
        private final transient Collection<DependableTerm.Dependency> deps =
            new LinkedList<DependableTerm.Dependency>();
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            final StringBuilder text = new StringBuilder();
            text.append('[');
            int total = 0;
            for (Long msg : this.msgs) {
                if (++total > 1) {
                    text.append(',');
                }
                text.append(msg);
                // @checkstyle MagicNumber (1 line)
                if (total > 20) {
                    text.append("...");
                    break;
                }
            }
            return text.append(']').toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean dependsOn(final DependableTerm.Dependency dep) {
            boolean depends = false;
            for (DependableTerm.Dependency tdep : this.deps) {
                if (tdep.matches(dep)) {
                    depends = true;
                    break;
                }
            }
            return depends;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public long fetch(final Term term, final Cursor cursor) {
            synchronized (this.deps) {
                if (this.msgs == null) {
                    this.prefetch(term, cursor);
                }
            }
            long msg = 0;
            if (!cursor.end() && cursor.msg().number() > 1) {
                final Iterator<Long> iterator =
                    this.msgs.tailSet(cursor.msg().number() - 1).iterator();
                if (iterator.hasNext()) {
                    msg = iterator.next();
                } else {
                    msg = this.tail(term, cursor);
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
            this.msgs.add(msg);
            return msg;
        }
        /**
         * Prefetch the head of the row.
         * @param term The term to shift
         * @param cursor Cursor to use
         */
        private void prefetch(final Term term, final Cursor cursor) {
            this.msgs = new ConcurrentSkipListSet<Long>(
                Collections.reverseOrder()
            );
            long msg = Long.MAX_VALUE;
            Cursor shifted = cursor;
            while (!shifted.end() && msg > cursor.msg().number()) {
                shifted = term.shift(shifted);
                if (shifted.end()) {
                    msg = 0L;
                } else {
                    msg = shifted.msg().number();
                }
                this.msgs.add(msg);
            }
            if (term instanceof DependableTerm) {
                this.deps.addAll(
                    DependableTerm.class.cast(term).dependencies()
                );
            }
        }
    }

    /**
     * Clear all terms that depend on this dep.
     * @param dep The dependency to use as matcher
     */
    private void clear(final DependableTerm.Dependency dep) {
        final DefaultCache.Numbers remover = new DefaultCache.Numbers() {
            @Override
            public boolean equals(final Object data) {
                return DefaultCache.Numbers.class
                    .cast(data).dependsOn(dep);
            }
            @Override
            public int hashCode() {
                return super.hashCode() + 1;
            }
            @Override
            public boolean dependsOn(
                final DependableTerm.Dependency dep) {
                throw new UnsupportedOperationException();
            }
            @Override
            public long fetch(final Term term, final Cursor cursor) {
                throw new UnsupportedOperationException();
            }
        };
        for (Term term : this.cached.keySet()) {
            this.cached.remove(term, remover);
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
        this.cached.putIfAbsent(
            term,
            new DefaultCache.RealNumbers()
        );
        final Numbers numbers = this.cached.get(term);
        long msg;
        if (numbers == null) {
            msg = this.through(term, cursor);
        } else {
            msg = numbers.fetch(term, cursor);
        }
        return msg;
    }

}
