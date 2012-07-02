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
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * Backlog in a directory.
 *
 * <p>Class is thread-safe for reading and NOT thread-safe for writing
 * operations.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
class Backlog {

    /**
     * Marker at the end of file (also visible from {@code Catalog.SlowLow}).
     */
    protected static final String EOF_MARKER = "EOF";

    /**
     * Marker of the file start.
     */
    protected static final int START_MARKER = 0x00ABCDEF;

    /**
     * Length of EOF marker, in bytes.
     */
    private static int eofMarkerLength;

    /**
     * Calculate the size of {@code writeUTF()} for EOF marker.
     */
    static {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final DataOutputStream data = new DataOutputStream(stream);
        try {
            data.writeUTF(Backlog.EOF_MARKER);
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
        Backlog.eofMarkerLength = stream.toByteArray().length;
    }

    /**
     * Main file.
     */
    private final transient File ifile;

    /**
     * Public ctor.
     * @param bck The back log
     * @throws IOException If some I/O problem inside
     */
    public Backlog(final File bck) throws IOException {
        this.ifile = bck;
        FileUtils.touch(this.ifile);
        if (this.ifile.length() == 0) {
            final DataOutputStream data = new DataOutputStream(
                new FileOutputStream(this.ifile)
            );
            data.writeInt(Backlog.START_MARKER);
            data.writeUTF(Backlog.EOF_MARKER);
            data.writeUTF(Backlog.EOF_MARKER);
            data.close();
            Logger.debug(
                this,
                "#Backlog('%s'): started",
                FilenameUtils.getName(this.ifile.getPath())
            );
        }
    }

    /**
     * One item.
     *
     * <p>The class is immutable and thread-safe;
     */
    public static final class Item implements Comparable<Backlog.Item> {
        /**
         * Value.
         */
        private final transient String val;
        /**
         * Name of the file.
         */
        private final transient String name;
        /**
         * Public ctor.
         * @param value Value
         * @param path Path to the file
         */
        public Item(final String value, final String path) {
            this.val = value;
            this.name = path;
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
         * Get value.
         * @return The value
         */
        public String value() {
            return this.val;
        }
        /**
         * Get reference.
         * @return The reference
         */
        public String path() {
            return this.name;
        }
    }

    /**
     * Register new reference in the backlog.
     *
     * <p>The method is NOT thread-safe.
     *
     * @param item The item to add
     * @return Position in file where this item is added
     * @throws IOException If some I/O problem inside
     */
    public final long add(final Item item) throws IOException {
        final RandomAccessFile data = new RandomAccessFile(this.ifile, "rw");
        long pos;
        try {
            data.seek(this.ifile.length() - Backlog.eofMarkerLength * 2);
            pos = data.getFilePointer();
            data.writeUTF(item.value());
            data.writeUTF(item.path());
            data.writeUTF(Backlog.EOF_MARKER);
            data.writeUTF(Backlog.EOF_MARKER);
        } finally {
            data.close();
        }
        Logger.debug(
            this,
            "#add('%[text]s', '%s'): added at pos #%d",
            item.value(),
            item.path(),
            pos
        );
        return pos;
    }

    /**
     * Get an iterator of them items.
     * @return The thread-safe iterator
     * @throws IOException If some I/O problem inside
     */
    public Iterator<Backlog.Item> iterator() throws IOException {
        return new Backlog.ItemsIterator();
    }

    /**
     * Get name of the file we're working with.
     * @return The file name
     * @throws IOException If some I/O problem inside
     */
    protected final File file() throws IOException {
        return this.ifile;
    }

    /**
     * Iterator of backlog items.
     */
    private final class ItemsIterator implements Iterator<Backlog.Item> {
        /**
         * Data input.
         */
        private final transient DataInputStream data;
        /**
         * Recent item.
         */
        private final transient AtomicReference<Backlog.Item> item =
            new AtomicReference<Backlog.Item>();
        /**
         * EOF flag.
         */
        private final transient AtomicBoolean eof = new AtomicBoolean();
        /**
         * Public ctor.
         * @throws IOException If some I/O problem inside
         */
        public ItemsIterator() throws IOException {
            this.data = new DataInputStream(
                new FileInputStream(Backlog.this.ifile)
            );
            if (this.data.readInt() != Backlog.START_MARKER) {
                throw new IllegalArgumentException("wrong file format");
            }
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            if (this.item.get() == null && !this.eof.get()) {
                try {
                    final Item next =
                        new Item(this.data.readUTF(), this.data.readUTF());
                    if (next.value().equals(Backlog.EOF_MARKER)
                        && next.path().equals(Backlog.EOF_MARKER)) {
                        this.eof.set(true);
                        this.data.close();
                    } else {
                        this.item.set(next);
                    }
                } catch (java.io.IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            return !this.eof.get();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Item next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException(
                    "no more elements in backlog"
                );
            }
            return this.item.getAndSet(null);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("#remove");
        }
    }

}
