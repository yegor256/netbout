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

import java.util.SortedSet;

/**
 * Watching index map.
 *
 * <p>This class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class WatchingIndexMap implements IndexMap {

    /**
     * The original map.
     */
    private final transient IndexMap origin;

    /**
     * The records to use.
     */
    private final transient Records records;

    /**
     * Public ctor.
     * @param map Original map
     * @param rcds Records to use
     */
    public WatchingIndexMap(final IndexMap map, final Records rcds) {
        this.origin = map;
        this.records = rcds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Index index(final String attr) {
        return new WatchingIndex(this.origin.index(attr), attr, this.records);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void touch(final long number) {
        this.origin.touch(number);
        this.records.add("touch", number);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Long> msgs() {
        return this.origin.msgs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long maximum() {
        return this.origin.maximum();
    }

}
