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
package com.netbout.inf;

import java.util.Arrays;
import java.util.Collection;

/**
 * Segments of a term.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class Segments {

    /**
     * Full coverage.
     */
    public static final Segments ALWAYS = new Segments(
        Arrays.asList(new Long[] {0L, Long.MAX_VALUE})
    );

    /**
     * No coverage at all.
     */
    public static final Segments NEVER = new Segments(
        Arrays.asList(new Long[0])
    );

    /**
     * Create with all these numbers in the segments.
     * @param numbers The numbers to add
     */
    public Segments(final Collection<Long> numbers) {
        // todo
    }

    /**
     * Join them all together (AND).
     * @param terms The terms to merge
     * @return New segments
     */
    public static Segments conjunction(final Collection<Term> terms) {
        return Segments.ALWAYS;
    }

    /**
     * Join them all together (OR).
     * @param terms The terms to merge
     * @return New segments
     */
    public static Segments disjunction(final Collection<Term> terms) {
        return Segments.ALWAYS;
    }

    /**
     * Reverse this segments.
     * @return New segments, reversed
     */
    public Segments reverse() {
        return Segments.ALWAYS;
    }

    /**
     * Correct this cursor and return a new one, which is more likely to
     * match one of the numbers in the segment.
     * @param cursor The cursor to start from
     * @return The new cursor
     */
    public Cursor correct(final Cursor cursor) {
        return cursor;
    }

}
