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
import java.util.HashSet;
import java.util.Set;

/**
 * NOT term.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Cacheable
final class NotTerm implements DependableTerm {

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
    public Set<DependableTerm.Dependency> dependencies() {
        final Set<DependableTerm.Dependency> deps =
            new HashSet<DependableTerm.Dependency>();
        if (this.term instanceof DependableTerm) {
            deps.addAll(DependableTerm.class.cast(this.term).dependencies());
        }
        return deps;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        text.append("(NOT ").append(this.term).append(')');
        return text.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor shift(final Cursor cursor) {
        Cursor shifted = cursor;
        Cursor candidate = cursor;
        final Term always = new AlwaysTerm(this.imap);
        while (!shifted.end()) {
            candidate = shifted.shift(always);
            shifted = this.term.shift(shifted);
            if (shifted.compareTo(candidate) < 0) {
                break;
            }
        }
        return candidate;
    }

}
