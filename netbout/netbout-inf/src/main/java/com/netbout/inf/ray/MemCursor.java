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
import com.netbout.inf.Msg;
import com.netbout.inf.Term;

/**
 * In-memory implementation of {@link Cursor}.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class MemCursor implements Cursor {

    /**
     * Message number where we're staying now.
     */
    private final transient long msg;

    /**
     * Index map.
     */
    private final transient IndexMap imap;

    /**
     * Public ctor.
     * @param num Message number
     * @param map The index map
     */
    public MemCursor(final long num, final IndexMap map) {
        this.msg = num;
        this.imap = map;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        text.append("cursor-");
        if (this.msg == Long.MAX_VALUE) {
            text.append("TOP");
        } else if (this.msg == 0L) {
            text.append("END");
        } else {
            text.append(this.msg);
        }
        return text.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Cursor cursor) {
        Long number;
        if (cursor.end()) {
            number = 0L;
        } else {
            number = cursor.msg().number();
        }
        return Long.valueOf(this.msg).compareTo(number);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final Term term, final String attr, final String value) {
        this.update(
            new Updater() {
                @Override
                public void update(final Index index, final long msg) {
                    index.add(msg, value);
                }
            },
            attr,
            term
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replace(final Term term, final String attr,
        final String value) {
        this.update(
            new Updater() {
                @Override
                public void update(final Index index, final long msg) {
                    index.replace(msg, value);
                }
            },
            attr,
            term
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final Term term, final String attr) {
        this.update(
            new Updater() {
                @Override
                public void update(final Index index, final long msg) {
                    index.delete(msg);
                }
            },
            attr,
            term
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final Term term, final String attr, final String value) {
        this.update(
            new Updater() {
                @Override
                public void update(final Index index, final long msg) {
                    index.delete(msg, value);
                }
            },
            attr,
            term
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor shift(final Term term) {
        return term.shift(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor copy() {
        return new MemCursor(this.msg, this.imap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Msg msg() {
        if (this.end()) {
            throw new IllegalStateException("end of cursor reached");
        }
        return new Msg() {
            @Override
            public long number() {
                return MemCursor.this.msg;
            }
            @Override
            public String first(final String name) {
                return MemCursor.this.imap.index(name)
                    .values(this.number())
                    .iterator()
                    .next();
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean end() {
        return this.msg == 0L;
    }

    /**
     * Manipulate on index.
     * @param man The manipulator to use
     * @param attr The attribute
     * @param term The term
     */
    private void update(final MemCursor.Updater updater,
        final String attr, final Term term) {
        final Index index = this.imap.index(attr);
        long num = this.msg;
        Cursor cursor = this;
        while (!cursor.end()) {
            updater.update(index, cursor.msg().number());
            cursor = cursor.shift(term);
        }
    }

    /**
     * Operation to do on index.
     */
    private interface Updater {
        /**
         * Update index.
         * @param index The index
         * @param attr The attribute
         */
        void update(Index index, long msg);
    }

}
