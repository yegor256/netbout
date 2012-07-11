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
package com.netbout.inf.ray.imap.dir;

import com.jcabi.log.Logger;
import com.netbout.inf.Lattice;
import com.netbout.inf.lattice.LatticeBuilder;
import com.netbout.inf.ray.imap.Numbers;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
public class SimpleNumbers implements Numbers {

    /**
     * Set of numbers.
     */
    private final transient SortedSet<Long> nums =
        new ConcurrentSkipListSet<Long>(Collections.reverseOrder());

    /**
     * Lattice.
     */
    private final transient LatticeBuilder lat = new LatticeBuilder().never();

    /**
     * {@inheritDoc}
     */
    @Override
    public final long sizeof() {
        return this.nums.size() * Numbers.SIZE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Lattice lattice() {
        return this.lat.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void add(final long number) {
        this.nums.add(number);
        this.lat.set(number, true, this.nums);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void remove(final long number) {
        this.nums.remove(number);
        this.lat.set(number, false, this.nums);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final long next(final long number) {
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
    public final long save(final OutputStream stream) throws IOException {
        final DataOutputStream data = new DataOutputStream(stream);
        long size = 0;
        for (Long number : this.nums) {
            data.writeLong(number);
            size += Numbers.SIZE;
        }
        data.writeLong(0L);
        size += Numbers.SIZE;
        data.flush();
        Logger.debug(
            this,
            "#save(..): saved %d numbers (%d bytes)",
            this.nums.size(),
            size
        );
        return size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void load(final InputStream stream) throws IOException {
        this.nums.clear();
        final DataInputStream data = new DataInputStream(stream);
        long previous = Long.MAX_VALUE;
        while (true) {
            final long next = data.readLong();
            if (next > previous) {
                throw new IOException("invalid order of numbers");
            }
            if (next == 0) {
                break;
            }
            this.nums.add(next);
            previous = next;
        }
        if (!this.nums.isEmpty()) {
            Logger.debug(
                this,
                "#load(..): loaded %d numbers",
                this.nums.size()
            );
            this.lat.fill(this.nums);
        }
    }

}
