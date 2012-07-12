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
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * Catalog in a directory.
 *
 * <p>Class is thread-safe for reading and NOT thread-safe for writing
 * operations.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
final class Catalog {

    /**
     * Index file, for fast search.
     */
    private final transient File fast;

    /**
     * Full file for slow search.
     */
    private final transient Slowlog slow;

    /**
     * Public ctor.
     * @param ctlg The file to use
     * @throws IOException If some I/O problem inside
     */
    public Catalog(final File ctlg) throws IOException {
        this.fast = ctlg;
        FileUtils.touch(this.fast);
        this.slow = new Slowlog(
            new File(
                this.fast.getParentFile(),
                String.format(
                    "%s-slow.%s",
                    FilenameUtils.getBaseName(this.fast.getPath()),
                    FilenameUtils.getExtension(this.fast.getPath())
                )
            )
        );
    }

    /**
     * One item.
     *
     * <p>The class is immutable and thread-safe;
     */
    public static final class Item implements Comparable<Catalog.Item> {
        /**
         * Size of Item in bytes (INT + LONG).
         * @checkstyle MagicNumber (2 lines)
         */
        public static final int SIZE = 4 + 8;
        /**
         * Value.
         */
        private final transient String val;
        /**
         * Position.
         */
        private final transient long pos;
        /**
         * Public ctor.
         * @param value The value
         * @param postn The position
         */
        public Item(final String value, final long postn) {
            this.val = value;
            this.pos = postn;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return this.val.hashCode();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(final Object item) {
            return this == item || (item instanceof Item
                && item.hashCode() == this.hashCode());
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(final Item item) {
            return new Integer(this.hashCode()).compareTo(
                new Integer(item.hashCode())
            );
        }
        /**
         * Get value.
         * @return The value
         */
        public String value() {
            return this.val;
        }
        /**
         * Get position.
         * @return The pos
         */
        public long position() {
            return this.pos;
        }
    }

    /**
     * Get position of numbers in data file, for the given value,
     * or less than ZERO if such a value is not found in catalog.
     *
     * <p>The method is thread-safe.
     *
     * @param value The value
     * @return Position in data file or -1 if not found
     * @throws IOException If some I/O problem inside
     */
    public long seek(final String value) throws IOException {
        final int target = value.hashCode();
        // @checkstyle MultipleStringLiterals (1 line)
        final RandomAccessFile data = new RandomAccessFile(this.fast, "r");
        long left = 0;
        long right = data.length() / Catalog.Item.SIZE;
        long found = Long.MIN_VALUE;
        int hops = 0;
        while (left < right) {
            final long pos = left + (right - left) / 2;
            data.seek(pos * Catalog.Item.SIZE);
            final int hash = data.readInt();
            if (hash == target) {
                found = this.slow.normalized(data.readLong(), value);
                break;
            }
            if (hash < target && left < right - 1) {
                left = pos;
            } else {
                right = pos;
            }
            ++hops;
        }
        if (found > 0) {
            Logger.debug(
                this,
                "#seek('%[text]s'): found pos #%d among %d value(s) in %d hops",
                value,
                found,
                data.length() / Catalog.Item.SIZE,
                hops
            );
        }
        data.close();
        return found;
    }

    /**
     * Create it from scratch, using the provided items.
     *
     * <p>The method is NOT thread-safe.
     *
     * @param items The items to use
     * @throws IOException If some I/O problem inside
     * @checkstyle ExecutableStatementCount (100 lines)
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void create(final Iterator<Catalog.Item> items) throws IOException {
        final long start = System.currentTimeMillis();
        final CatalogOutputStream output = new CatalogOutputStream(this.fast);
        final BacklogOutputStream slowlog = this.slow.open();
        int total = 0;
        int dups = 0;
        try {
            int previous = Integer.MIN_VALUE;
            long dupstart = Integer.MIN_VALUE;
            while (items.hasNext()) {
                final Item item = items.next();
                final int hash = item.hashCode();
                if (hash < previous) {
                    throw new IllegalArgumentException(
                        String.format(
                            // @checkstyle LineLength (1 line)
                            "item:('%s', #%d) at wrong ordering position (%d total, %d dups)",
                            item.value(),
                            item.position(),
                            total,
                            dups
                        )
                    );
                }
                final long pos = slowlog.write(
                    new Slowlog.Item(
                        item.value(),
                        Long.toString(item.position())
                    )
                );
                if (hash == previous) {
                    output.back();
                    output.write(new Catalog.Item(item.value(), -dupstart));
                    Logger.debug(this, "#create(): duplicate '0x%08X'", hash);
                    ++dups;
                } else {
                    dupstart = pos;
                    output.write(item);
                    ++total;
                }
                previous = hash;
            }
        } finally {
            output.close();
            slowlog.close();
        }
        Logger.debug(
            this,
            // @checkstyle LineLength (1 line)
            "#create(): saved to %s (%s bytes, %d values, %d dups) in %[ms]s",
            FilenameUtils.getName(this.fast.getPath()),
            FileUtils.byteCountToDisplaySize(this.fast.length()),
            total,
            dups,
            System.currentTimeMillis() - start
        );
    }

    /**
     * Get an iterator of them items.
     * @return The thread-safe iterator
     * @throws IOException If some I/O problem inside
     */
    public Iterator<Catalog.Item> iterator() throws IOException {
        final Iterator<Slowlog.Item> origin = this.slow.iterator();
        return new Iterator<Catalog.Item>() {
            @Override
            public boolean hasNext() {
                return origin.hasNext();
            }
            @Override
            public Item next() {
                final Slowlog.Item item = origin.next();
                return new Item(item.value(), Long.valueOf(item.path()));
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException("#remove");
            }
        };
    }

}
