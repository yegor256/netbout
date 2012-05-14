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

import java.util.Set;
import java.util.SortedSet;

/**
 * Watching ndex.
 *
 * <p>This class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class WatchingIndex implements Index {

    /**
     * The original index.
     */
    private final transient Index origin;

    /**
     * Name of attribute.
     */
    private final transient String attr;

    /**
     * The records to use.
     */
    private final transient Records records;

    /**
     * Public ctor.
     * @param index Original index
     * @param name Attribute name
     * @param rcds Records to use
     */
    public WatchingIndex(final Index index, final String name,
        final Records rcds) {
        this.origin = index;
        this.attr = name;
        this.records = rcds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replace(final long msg, final String value) {
        this.origin.replace(msg, value);
        this.records.add("replace", this.attr, msg, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final long msg, final String value) {
        this.origin.add(msg, value);
        this.records.add("add", this.attr, msg, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final long msg, final String value) {
        this.origin.delete(msg, value);
        this.records.add("delete", this.attr, msg, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clean(final long msg) {
        this.origin.clean(msg);
        this.records.add("clean", this.attr, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> values(final long msg) {
        return this.origin.values(msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Long> msgs(final String value) {
        return this.origin.msgs(value);
    }

}
