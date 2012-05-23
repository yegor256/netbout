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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * NOT term for negating of MATCHER.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class NotMatcherTerm implements Term {

    /**
     * Name of attribute (also visible from {@link AndTerm}).
     * @checkstyle VisibilityModifier (3 lines)
     */
    @SuppressWarnings("PMD.AvoidProtectedFieldInFinalClass")
    protected final transient String attr;

    /**
     * Values to exclude (visible from {@link AndTerm}).
     * @checkstyle VisibilityModifier (3 lines)
     */
    @SuppressWarnings("PMD.AvoidProtectedFieldInFinalClass")
    protected final transient Set<String> values = new LinkedHashSet<String>();

    /**
     * Index map.
     */
    private final transient IndexMap imap;

    /**
     * Public ctor.
     * @param map The index map
     * @param term The terms to negate
     */
    public NotMatcherTerm(final IndexMap map, final MatcherTerm term) {
        this.imap = map;
        this.attr = term.attr;
        this.values.add(term.value);
    }

    /**
     * Public ctor.
     * @param map The index map
     * @param name Name of attribute
     * @param vals Values to exclude
     */
    public NotMatcherTerm(final IndexMap map, final String name,
        final Collection<String> vals) {
        this.imap = map;
        if (vals.isEmpty()) {
            throw new IllegalArgumentException("empty list of values");
        }
        this.attr = name;
        this.values.addAll(vals);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Logger.format("(NOT %s %[list]s)", this.attr, this.values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor shift(final Cursor cursor) {
        Cursor shifted;
        if (cursor.end()) {
            shifted = cursor;
        } else {
            final long current = cursor.msg().number();
            shifted = new MemCursor(
                this.next(
                    this.imap.msgs().tailSet(current).iterator(),
                    current
                ),
                this.imap
            );
        }
        Logger.debug(this, "#shift(%s): to %s", cursor, shifted);
        return shifted;
    }

    /**
     * Get next number from iterator, which is not equal to the provided one
     * (exlude the values from the Set).
     * @param iterator The iterator
     * @param ignore The number to ignore
     * @return The number found or ZERO if nothing found
     */
    private long next(final Iterator<Long> iterator, final long ignore) {
        long next = 0L;
        final Collection<Set<Long>> excludes = new ArrayList<Set<Long>>();
        for (String value : this.values) {
            excludes.add(this.imap.index(this.attr).msgs(value));
        }
        while (iterator.hasNext()) {
            next = iterator.next();
            if (next == ignore) {
                next = 0L;
                continue;
            }
            boolean exclude = false;
            for (Set<Long> msgs : excludes) {
                if (msgs.contains(next)) {
                    exclude = true;
                    break;
                }
            }
            if (exclude) {
                next = 0L;
                continue;
            }
            break;
        }
        return next;
    }

}
