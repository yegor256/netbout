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
package com.netbout.inf.ray.imap;

import com.jcabi.log.Logger;
import com.netbout.inf.Lattice;
import com.netbout.inf.lattice.LatticeBuilder;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Simple implemenation of {@link Numbers}.
 *
 * <p>The class is thread-safe, except {@link #load(InputStream)}
 * and {@link #save(OutputStream)} methods.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class SimpleNumbers implements Numbers {

    /**
     * Set of numbers.
     */
    private final transient SortedSet<Long> nums =
        new ConcurrentSkipListSet<Long>(Collections.reverseOrder());

    /**
     * Lattice.
     */
    private transient Lattice lat;

    /**
     * Default ctor.
     */
    public SimpleNumbers() {
        this.lat = new LatticeBuilder().never().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Lattice lattice() {
        return this.lat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final long number) {
        this.nums.add(number);
        this.lat = new LatticeBuilder()
            .copy(this.lat)
            .set(number, true, this.nums)
            .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final long number) {
        this.nums.remove(number);
        this.lat = new LatticeBuilder()
            .copy(this.lat)
            .set(number, false, this.nums)
            .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long next(final long number) {
        long next = 0L;
        final Iterator<Long> tail = this.nums.tailSet(number - 1).iterator();
        if (tail.hasNext()) {
            next = tail.next();
        }
        return next;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(final OutputStream stream) throws IOException {
        final DataOutputStream data = new DataOutputStream(stream);
        for (Long number : this.nums) {
            data.writeLong(number);
        }
        data.writeLong(0L);
        data.flush();
        Logger.debug(
            this,
            "#save(..): saved %d numbers %[list]s",
            this.nums.size(),
            this.nums
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(final InputStream stream) throws IOException {
        this.nums.clear();
        final DataInputStream data = new DataInputStream(stream);
        while (data.available() > 0) {
            final long next = data.readLong();
            if (next == 0) {
                break;
            }
            this.nums.add(next);
        }
        Logger.debug(
            this,
            "#load(..): loaded %d numbers %[list]s",
            this.nums.size(),
            this.nums
        );
    }

}
