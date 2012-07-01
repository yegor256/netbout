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
 */
final class Catalog {

    /**
     * Index file, for fast search.
     */
    private final transient File fast;

    /**
     * Full file for slow search.
     */
    private final transient Catalog.SlowLog slow;

    /**
     * Public ctor.
     * @param ctlg The file to use
     * @throws IOException If some I/O problem inside
     */
    public Catalog(final File ctlg) throws IOException {
        this.fast = ctlg;
        FileUtils.touch(this.fast);
        this.slow = new Catalog.SlowLog(
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
        }
        if (found > 0) {
            Logger.debug(
                this,
                "#seek('%[text]s'): found pos #%d among %d value(s)",
                value,
                found,
                data.length() / Catalog.Item.SIZE
            );
        } else {
            Logger.debug(
                this,
                "#seek('%[text]s'): 0x%08X not found among %d value(s)",
                value,
                target,
                data.length() / Catalog.Item.SIZE
            );
        }
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
    public void create(final Iterator<Catalog.Item> items) throws IOException {
        final long start = System.currentTimeMillis();
        final RandomAccessFile ffile = new RandomAccessFile(this.fast, "rw");
        ffile.setLength(0L);
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
                            "item:('%s', #%d) at wrong ordering position",
                            item.value(),
                            item.position()
                        )
                    );
                }
                final long pos = this.slow.add(
                    new SlowLog.Item(
                        item.value(),
                        Long.toString(item.position())
                    )
                );
                if (hash == previous) {
                    ffile.seek(ffile.getFilePointer() - Catalog.Item.SIZE);
                    ffile.writeInt(hash);
                    ffile.writeLong(-dupstart);
                    Logger.debug(this, "#create(): duplicate '0x%08X'", hash);
                    ++dups;
                } else {
                    dupstart = pos;
                    ffile.writeInt(hash);
                    ffile.writeLong(item.position());
                    ++total;
                }
                previous = hash;
            }
        } finally {
            ffile.close();
        }
        Logger.debug(
            this,
            // @checkstyle LineLength (1 line)
            "#create(): saved to %s (%d bytes, %d values, %d dups) in %[ms]s",
            FilenameUtils.getName(this.fast.getPath()),
            this.fast.length(),
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
        final Iterator<Catalog.SlowLog.Item> origin = this.slow.iterator();
        return new Iterator<Catalog.Item>() {
            @Override
            public boolean hasNext() {
                return origin.hasNext();
            }
            @Override
            public Item next() {
                final Catalog.SlowLog.Item item = origin.next();
                return new Item(item.value(), Long.valueOf(item.path()));
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException("#remove");
            }
        };
    }

    /**
     * Slow log.
     */
    private static final class SlowLog extends Backlog {
        /**
         * Public ctor.
         * @param file The file to use
         * @throws IOException If some I/O problem inside
         */
        public SlowLog(final File file) throws IOException {
            super(file);
        }
        /**
         * Convert position to the normal form.
         *
         * <p>If position is negative it means that we should do a full search
         * in slow index, by its UTF value.
         *
         * @param pos Position found in fast index
         * @param value The value
         * @return Normalized position in data file
         * @throws IOException If some I/O problem inside
         */
        public long normalized(final long pos,
            final String value) throws IOException {
            long norm;
            if (pos < 0) {
                norm = Long.valueOf(this.seek(-pos, value));
            } else {
                norm = pos;
            }
            return norm;
        }
        /**
         * Find ref by value, starting with given position.
         * @param pos Position to start with
         * @param value The value to search for
         * @return Reference found
         * @throws IOException If some I/O problem inside
         */
        private String seek(final long pos,
            final String value) throws IOException {
            final RandomAccessFile data =
                new RandomAccessFile(this.file(), "r");
            data.seek(pos);
            String ref;
            while (true) {
                final String val = data.readUTF();
                if (val.equals(Backlog.EOF_MARKER)) {
                    throw new IllegalArgumentException(
                        String.format(
                            "value '%s' not found in slow index",
                            value
                        )
                    );
                }
                ref = data.readUTF();
                if (val.equals(value)) {
                    Logger.debug(
                        this,
                        "#seek(%d, '%[text]s'): found pos #%s in slow search",
                        pos,
                        value,
                        ref
                    );
                    break;
                }
            }
            return ref;
        }
    }

}
