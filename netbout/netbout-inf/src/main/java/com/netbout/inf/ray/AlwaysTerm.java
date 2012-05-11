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
import java.util.Iterator;

/**
 * Always term.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class AlwaysTerm implements Term {

    /**
     * Index map.
     */
    private final transient IndexMap imap;

    /**
     * Public ctor.
     * @param map The index map
     */
    public AlwaysTerm(final IndexMap map) {
        this.imap = map;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "(ALWAYS)";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Cursor shift(final Cursor cursor) {
        Cursor shifted = cursor;
        if (!shifted.end()) {
            shifted = new MemCursor(
                this.next(shifted.msg().number()),
                this.imap
            );
        }
        Logger.debug(this, "#shift(%s): %s to %s", cursor, this, shifted);
        return shifted;
    }

    /**
     * Get next message number after this one.
     * @param number The number to use
     * @return Next one or zero if there is nothing else
     */
    private long next(final long number) {
        final Iterator<Long> tail = this.imap.msgs().tailSet(number).iterator();
        long next = 0L;
        if (tail.hasNext()) {
            next = tail.next();
            if (next == number) {
                // @checkstyle NestedIfDepth (4 lines)
                if (tail.hasNext()) {
                    next = tail.next();
                } else {
                    next = 0L;
                }
            }
        }
        return next;
    }

}
