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
 * this code accidentally and without intent to use it, please report this
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
import com.netbout.inf.lattice.Feeder;
import com.netbout.inf.lattice.LatticeBuilder;
import com.netbout.inf.lattice.Range;
import com.netbout.inf.ray.imap.Numbers;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.validation.constraints.NotNull;

/**
 * Fast implemenation of {@link Numbers}.
 *
 * <p>The class is thread-safe, except {@link #load(InputStream)}
 * and {@link #save(OutputStream)} methods.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.TooManyMethods" })
public class FastNumbers implements Numbers {

    /**
     * Synchronization mutex.
     */
    private final transient Integer mutex = new Integer(1);

    /**
     * Array of numbers and mnemos, sorted in reverse order.
     *
     * <p>The last element is always ZERO.
     */
    private transient long[] nums = new long[] {0L};

    /**
     * Total number of numbers in the array, including the trailing ZERO.
     */
    private transient int size = 1;

    /**
     * Lattice.
     */
    private final transient LatticeBuilder lat = new LatticeBuilder().never();

    /**
     * Range for lattice.
     */
    private final transient Range range = new Range() {
        @Override
        public int window(final long head, final long tail) {
            synchronized (FastNumbers.this.mutex) {
                return FastNumbers.this.find(tail)
                    - FastNumbers.this.find(head);
            }
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public final long sizeof() {
        return this.nums.length * Numbers.SIZE;
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
    public final boolean isEmpty() {
        return this.size <= 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void add(final long number) {
        if (number == Long.MAX_VALUE) {
            throw new IllegalArgumentException("can't add MAX_VALUE");
        }
        if (number == 0L) {
            throw new IllegalArgumentException("can't add ZERO");
        }
        synchronized (this.mutex) {
            final int pos = this.find(number);
            if (this.nums[pos] != Long.MAX_VALUE && this.nums[pos] != number) {
                this.resize();
                System.arraycopy(
                    this.nums,
                    pos,
                    this.nums,
                    pos + 1,
                    this.size - pos
                );
                this.nums[pos] = number;
                ++this.size;
                this.compress(pos);
            }
            assert this.sanity() : "sanity check failure after adding";
        }
        this.lat.update(number, this.range);
    }

    /**
     * {@inheritDoc}
     * @checkstyle IllegalToken (100 lines)
     * @checkstyle CyclomaticComplexity (100 lines)
     * @checkstyle ExecutableStatementCount (100 lines)
     * @checkstyle NestedIfDepth (100 lines)
     */
    @Override
    public final void remove(final long number) {
        if (number == Long.MAX_VALUE) {
            throw new IllegalArgumentException("can't remove MAX_VALUE");
        }
        if (number == 0L) {
            throw new IllegalArgumentException("can't remove ZERO");
        }
        synchronized (this.mutex) {
            // @checkstyle MagicNumber (1 line)
            final long[] insert = new long[4];
            int len = 0;
            final int pos = this.find(number);
            int head = 0;
            int cut = 0;
            boolean remove = true;
            if (this.nums[pos] == Long.MAX_VALUE) {
                assert pos > 0 && pos < this.size - 1;
                if (this.nums[pos - 1] > number + 2) {
                    insert[len++] = Long.MAX_VALUE;
                }
                if (this.nums[pos - 1] > number + 1) {
                    insert[len++] = number + 1;
                }
                if (this.nums[pos + 1] < number - 1) {
                    insert[len++] = number - 1;
                }
                if (this.nums[pos + 1] < number - 2) {
                    insert[len++] = Long.MAX_VALUE;
                }
            } else if (this.nums[pos] == number) {
                if (pos > 0 && this.nums[pos - 1] == Long.MAX_VALUE) {
                    if (this.nums[pos - 2] == number + 2) {
                        --head;
                    }
                    insert[len++] = number + 1;
                } else if (pos < this.size - 1
                    && this.nums[pos + 1] == Long.MAX_VALUE) {
                    if (this.nums[pos + 2] == number - 2) {
                        ++cut;
                    }
                    insert[len++] = number - 1;
                }
            } else {
                remove = false;
            }
            if (remove) {
                System.arraycopy(
                    this.nums,
                    pos + 1 + cut,
                    this.nums,
                    pos + head + len,
                    this.size - pos - cut
                );
                System.arraycopy(insert, 0, this.nums, pos + head, len);
                this.size += len - 1 - cut + head;
                this.resize();
                assert this.sanity() : "sanity check failure after removal";
            }
        }
        this.lat.update(number, this.range);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final long next(final long number) {
        synchronized (this.mutex) {
            final int pos = this.find(number);
            long next = this.nums[pos];
            if (next == number && next > 0) {
                next = this.nums[pos + 1];
            }
            if (next == Long.MAX_VALUE) {
                next = number - 1;
            }
            return next;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final long save(@NotNull final OutputStream stream)
        throws IOException {
        final DataOutputStream data = new DataOutputStream(stream);
        long written = 0;
        for (int pos = 0; pos < this.size; ++pos) {
            data.writeLong(this.nums[pos]);
            written += Numbers.SIZE;
        }
        data.flush();
        Logger.debug(
            this,
            "#save(..): saved %d bytes (this.size=%d)",
            written,
            this.size
        );
        return written;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void load(@NotNull final InputStream stream)
        throws IOException {
        this.size = 0;
        final DataInputStream data = new DataInputStream(stream);
        long previous = Long.MAX_VALUE;
        while (true) {
            final long next = data.readLong();
            if (next == previous) {
                throw new IOException(
                    String.format("duplicate number %d", next)
                );
            }
            if (next > previous && next != Long.MAX_VALUE) {
                throw new IOException(
                    String.format(
                        "invalid order of numbers: %d > %d", next, previous
                    )
                );
            }
            this.resize();
            this.nums[this.size] = next;
            ++this.size;
            if (next == 0) {
                break;
            }
            this.compress(this.size - 1);
            previous = next;
        }
        final Iterator<Long> iterator = new FastNumbers.FastIterator();
        this.lat.fill(
            new Feeder() {
                @Override
                public long next() {
                    long next;
                    if (iterator.hasNext()) {
                        next = iterator.next();
                    } else {
                        next = 0L;
                    }
                    return next;
                }
            }
        );
        Logger.debug(
            this,
            "#load(..): loaded %d numbers",
            this.size
        );
    }

    /**
     * Audit it against the reverse.
     * @param audit The audit
     * @param value The value these numbers are used for
     * @param reverse The reverse
     */
    public final void audit(@NotNull final Audit audit,
        @NotNull final String value, @NotNull final SimpleReverse reverse) {
        reverse.audit(
            audit,
            value,
            new Iterable<Long>() {
                @Override
                public Iterator<Long> iterator() {
                    return new FastNumbers.FastIterator();
                }
            }
        );
    }

    /**
     * Find position of NUM or any number that is the biggest
     * one of all numbers that are smaller than NUM.
     *
     * <p>It's a binary search, copied from {@code Arrays.binarySearch()}.
     *
     * <p>The method is NOT thread-safe, use {@code this.mutex}
     * every time you call it.
     *
     * @param num The number to look for
     * @return The position
     */
    private int find(final long num) {
        int head = 0;
        int tail = this.size - 1;
        int pos = 0;
        long val = 0L;
        while (head <= tail) {
            pos = (head + tail) >>> 1;
            val = this.nums[pos];
            if (val == Long.MAX_VALUE) {
                assert pos > 0 && pos < this.size - 1;
                if (this.nums[pos - 1] < num) {
                    val = this.nums[pos - 1] - 1;
                } else {
                    val = this.nums[pos + 1] + 1;
                }
            }
            if (val > num) {
                head = pos + 1;
            } else if (val < num) {
                tail = pos - 1;
            } else {
                break;
            }
        }
        if (head > tail) {
            pos = tail + 1;
        }
        return pos;
    }

    /**
     * Compress the underlying array, starting with the provided position, if
     * necessary and possible.
     *
     * <p>The method is NOT thread-safe, use {@code this.mutex}
     * every time you call it.
     *
     * @param pos The position to start with
     */
    private void compress(final int pos) {
        int head = pos;
        while (head > 0) {
            if (this.nums[head - 1] == this.nums[head] + 1) {
                --head;
                continue;
            }
            if (this.nums[head - 1] == Long.MAX_VALUE) {
                head -= 2;
                continue;
            }
            break;
        }
        int tail = pos;
        while (tail < this.size - 2) {
            if (this.nums[tail + 1] == this.nums[tail] - 1) {
                ++tail;
                continue;
            }
            if (this.nums[tail + 1] == Long.MAX_VALUE) {
                tail += 2;
                continue;
            }
            break;
        }
        if (tail - head > 2) {
            System.arraycopy(
                this.nums,
                tail,
                this.nums,
                head + 2,
                this.size - tail
            );
            this.size -= tail - head - 2;
            this.nums[head + 1] = Long.MAX_VALUE;
        }
    }

    /**
     * Add more space, if necessary, or shrink back.
     *
     * <p>The method is NOT thread-safe, use {@code this.mutex}
     * every time you call it.
     *
     * @checkstyle MagicNumber (10 lines)
     */
    private void resize() {
        final int len = ((this.size + 0x100) & 0xFFFFFF00) + 0x10;
        if (this.nums.length < len || this.nums.length > len + 0x200) {
            final long[] temp = new long[len];
            System.arraycopy(this.nums, 0, temp, 0, this.size);
            this.nums = temp;
        }
    }

    /**
     * Sanity check, for tests only.
     *
     * <p>The method is NOT thread-safe, use {@code this.mutex}
     * every time you call it.
     *
     * @return TRUE if everything is fine with the array
     */
    private boolean sanity() {
        boolean valid = true;
        final StringBuilder text = new StringBuilder().append('[');
        int pos = 0;
        for (; pos < this.nums.length; ++pos) {
            if (this.nums[pos] == Long.MAX_VALUE) {
                text.append("FF");
            } else {
                text.append(this.nums[pos]);
            }
            if (this.nums[pos] == 0) {
                break;
            }
            text.append(' ');
        }
        text.append(']');
        if (pos != this.size - 1) {
            valid = false;
            Logger.warn(this, "#sanity(): pos=#%d, size=%d", pos, this.size);
        }
        return valid;
    }

    /**
     * Iterator of all numbers, for internal needs.
     *
     * <p>The class is NOT thread-safe.
     */
    private final class FastIterator implements Iterator<Long> {
        /**
         * Current position.
         */
        private transient int pos;
        /**
         * Recently seen number.
         */
        private transient long recent = Long.MAX_VALUE;
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            return this.pos < FastNumbers.this.size - 1;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Long next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            long next = FastNumbers.this.nums[this.pos];
            ++this.pos;
            if (next == Long.MAX_VALUE) {
                next = this.recent - 1;
            }
            this.recent = next;
            return next;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
