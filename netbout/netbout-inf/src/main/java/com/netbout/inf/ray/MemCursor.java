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
import com.netbout.inf.Attribute;
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
@SuppressWarnings("PMD.TooManyMethods")
final class MemCursor implements Cursor {

    /**
     * Message number where we're staying now.
     */
    private final transient long where;

    /**
     * Index map.
     */
    private final transient IndexMap imap;

    /**
     * Which step we're at.
     */
    private final transient int step;

    /**
     * Public ctor.
     * @param num Message number
     * @param map The index map
     */
    public MemCursor(final long num, final IndexMap map) {
        this(num, map, 0);
    }

    /**
     * Public ctor.
     * @param num Message number
     * @param map The index map
     * @param stp Which step it is?
     */
    private MemCursor(final long num, final IndexMap map, final int stp) {
        this.where = num;
        this.imap = map;
        this.step = stp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        text.append("cursor-");
        if (this.where == Long.MAX_VALUE) {
            text.append("TOP");
        } else if (this.where == 0L) {
            text.append("END");
        } else {
            text.append(this.where);
        }
        return text.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Long.valueOf(this.where).hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object cursor) {
        return cursor == this || (cursor instanceof Cursor
            && cursor.hashCode() == this.hashCode());
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
        return Long.valueOf(this.where).compareTo(number);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final Term term, final Attribute attr, final String value) {
        this.update(
            new Updater() {
                @Override
                public void update(final Index index, final long num) {
                    index.add(num, value);
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
    public void replace(final Term term, final Attribute attr,
        final String value) {
        this.update(
            new Updater() {
                @Override
                public void update(final Index index, final long num) {
                    index.replace(num, value);
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
    public void delete(final Term term, final Attribute attr) {
        this.update(
            new Updater() {
                @Override
                public void update(final Index index, final long num) {
                    index.clean(num);
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
    public void delete(final Term term, final Attribute attr,
        final String value) {
        this.update(
            new Updater() {
                @Override
                public void update(final Index index, final long num) {
                    index.delete(num, value);
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
        if (this.end()) {
            throw new IllegalStateException("can't shift END cursor");
        }
        Cursor shifted;
        // @checkstyle MagicNumber (1 line)
        if (this.step > 10000) {
            Logger.warn(
                this,
                "#shift('%s'): shifted %d times already",
                term,
                this.step
            );
            shifted = new MemCursor(0L, this.imap, 0);
        } else if (term instanceof JumpTerm) {
            shifted = new MemCursor(
                JumpTerm.class.cast(term).msg(),
                this.imap,
                this.step + 1
            );
        } else {
            shifted = term.shift(this);
        }
        return shifted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor copy() {
        return new MemCursor(this.where, this.imap);
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
                return MemCursor.this.where;
            }
            @Override
            public String attr(final Attribute name) {
                try {
                    return MemCursor.this.imap.index(name).attr(this.number());
                } catch (java.io.IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean end() {
        return this.where == 0L;
    }

    /**
     * Manipulate on index.
     * @param updater The updater to use
     * @param attr The attribute
     * @param term The term
     */
    private void update(final MemCursor.Updater updater,
        final Attribute attr, final Term term) {
        Index index;
        try {
            index = this.imap.index(attr);
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
        Cursor cursor = this;
        if (!cursor.end() && cursor.msg().number() == Long.MAX_VALUE) {
            cursor = cursor.shift(term);
        }
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
         * @param msg The number of message to work with
         */
        void update(Index index, long msg);
    }

}
