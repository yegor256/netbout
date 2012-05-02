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
import java.util.Iterator;
import java.util.SortedSet;

/**
 * NOT term.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class NotTerm implements Term {

    /**
     * Index map.
     */
    private final transient IndexMap imap;

    /**
     * Term to negate.
     */
    private final transient Term term;

    /**
     * Public ctor.
     * @param map The index map
     * @param trm The term
     */
    public NotTerm(final IndexMap map, final Term trm) {
        this.imap = map;
        this.term = trm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor shift(final Cursor cursor) {
        Cursor shifted = cursor;
        Cursor candidate = cursor;
        while (!shifted.end()) {
            candidate = new MemCursor(
                this.next(shifted.msg().number()),
                this.imap
            );
            shifted = this.term.shift(shifted);
            if (shifted.compareTo(candidate) < 0) {
                break;
            }
        }
        return candidate;
    }

    /**
     * Get next message number after this one.
     * @param number The number to use
     * @return Next one or zero if there is nothing else
     */
    private long next(final long number) {
        final SortedSet<Long> tail = this.imap.msgs().tailSet(number);
        long next;
        if (tail.size() < 2) {
            next = 0L;
        } else {
            final Iterator<Long> iterator = tail.iterator();
            iterator.next();
            next = iterator.next();
        }
        return next;
    }

}
