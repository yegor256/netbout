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

/**
 * Default implementation of {@link TermBuilder}.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
final class MemTermBuilder implements TermBuilder {

    /**
     * Index map.
     */
    private final transient IndexMap imap;

    /**
     * Public ctor.
     * @param map The index map
     */
    public MemTermBuilder(final IndexMap map) {
        this.imap = map;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Term matcher(final String name, final String value) {
        return new MatcherTerm(this.imap, name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Term and(final Collection<Term> terms) {
        return new AndTerm(this.imap, terms);
    }

    /**
     * {@inheritDoc}
     * @checkstyle MethodName (4 lines)
     */
    @Override
    @SuppressWarnings("PMD.ShortMethodName")
    public Term or(final Collection<Term> terms) {
        return new OrTerm(this.imap, terms);
    }

    /**
     * {@inheritDoc}
     *
     * <p>This is an experimental implementation, which is going to speed up
     * the entire search engine. The only way we can speed it up is by using
     * a customized specifically-tailored term for NOT+MATCHER operation.
     */
    @Override
    public Term not(final Term term) {
        return new NotTerm(this.imap, term);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Term never() {
        return new Term() {
            @Override
            public Cursor shift(final Cursor cursor) {
                return new MemCursor(0L, MemTermBuilder.this.imap);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Term always() {
        return new AlwaysTerm(this.imap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Term picker(final long number) {
        return new PickerTerm(this.imap, number);
    }

}
