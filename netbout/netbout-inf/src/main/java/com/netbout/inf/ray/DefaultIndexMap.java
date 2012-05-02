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

import java.util.Collections;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Index map.
 *
 * <p>This class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class DefaultIndexMap implements IndexMap {

    /**
     * The map.
     */
    private final transient ConcurrentMap<String, Index> map =
        new ConcurrentHashMap<String, Index>();

    /**
     * All message numbers.
     */
    private final transient SortedSet<Long> all =
        new ConcurrentSkipListSet<Long>(Collections.reverseOrder());

    /**
     * {@inheritDoc}
     */
    @Override
    public Index index(final String attr) {
        if (this.map.get(attr) == null) {
            this.map.putIfAbsent(attr, new DefaultIndex());
        }
        return this.map.get(attr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void touch(final long number) {
        if (number == 0L) {
            throw new IllegalArgumentException("msg number can't be ZERO");
        }
        if (number == Long.MAX_VALUE) {
            throw new IllegalArgumentException("msg number can't be MAX_VALUE");
        }
        this.all.add(number);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Long> msgs() {
        return Collections.unmodifiableSortedSet(this.all);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long maximum() {
        long max;
        if (this.all.isEmpty()) {
            max = 0L;
        } else {
            max = this.all.first();
        }
        return max;
    }

}
