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
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Index.
 *
 * <p>This class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class DefaultIndex implements Index {

    /**
     * The map.
     */
    private final transient ConcurrentMap<String, SortedSet<Long>> map =
        new ConcurrentHashMap<String, SortedSet<Long>>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void replace(final long msg, final String value) {
        this.validate(msg);
        this.clean(msg);
        this.msgs(value).add(msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final long msg, final String value) {
        this.validate(msg);
        this.msgs(value).add(msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final long msg, final String value) {
        this.validate(msg);
        final SortedSet<Long> set = this.msgs(value);
        if (set.contains(msg)) {
            set.remove(msg);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clean(final long msg) {
        this.validate(msg);
        for (SortedSet<Long> set : this.map.values()) {
            if (set.contains(msg)) {
                set.remove(msg);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> values(final long msg) {
        this.validate(msg);
        final Set<String> values = new HashSet<String>();
        for (ConcurrentMap.Entry<String, SortedSet<Long>> entry
            : this.map.entrySet()) {
            if (entry.getValue().contains(msg)) {
                values.add(entry.getKey());
            }
        }
        return values;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Long> msgs(final String value) {
        if (this.map.get(value) == null) {
            this.map.putIfAbsent(
                value,
                new ConcurrentSkipListSet(Collections.reverseOrder())
            );
        }
        return this.map.get(value);
    }

    /**
     * Validate this message number and throw runtime exception if it's not
     * valid (is ZERO or MAX_VALUE).
     * @param msg The number of msg
     */
    private void validate(final long msg) {
        if (msg == 0L) {
            throw new IllegalArgumentException("msg number can't be ZERO");
        }
        if (msg == Long.MAX_VALUE) {
            throw new IllegalArgumentException("msg number can't be MAX_VALUE");
        }
    }

}
