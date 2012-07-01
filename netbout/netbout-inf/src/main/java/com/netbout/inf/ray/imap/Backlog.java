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
import com.netbout.inf.Attribute;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Backlog in a directory.
 *
 * <p>Class is thread-safe for reading and NOT thread-safe for writing
 * operations.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
class Backlog {

    /**
     * Marker at the beginning of the file.
     */
    private static final String START_MARKER = "BACKLOG";

    /**
     * Marker at the end of file.
     */
    protected static final String EOF_MARKER = "EOF";

    /**
     * Length of EOF marker, in bytes.
     */
    protected static int eofMarkerLength;

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
            final OutputStream stream = new FileOutputStream(this.ifile);
            final DataOutputStream data = new DataOutputStream(stream);
            data.writeUTF(Backlog.START_MARKER);
            data.writeUTF(Backlog.EOF_MARKER);
            stream.close();
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
    public static final class Item {
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
         * @param file Path to the file
         */
        public Item(final String value, final String path) {
            this.val = value;
            this.name = path;
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
     * @throws IOException If some I/O problem inside
     */
    public final void add(final Item item) throws IOException {
        final RandomAccessFile data = new RandomAccessFile(this.ifile, "rw");
        try {
            data.seek(this.ifile.length() - Backlog.eofMarkerLength);
            data.writeUTF(item.value());
            data.writeUTF(item.path());
            data.writeUTF(Backlog.EOF_MARKER);
        } finally {
            data.close();
        }
        Logger.debug(
            this,
            "#add('%[text]s', '%s'): added (file.length=%d)",
            item.value(),
            item.path(),
            this.file().length()
        );
    }

    /**
     * Get an iterator of them items.
     * @return The thread-safe iterator
     * @throws IOException If some I/O problem inside
     */
    public Iterator<Item> iterator() throws IOException {
        final FileInputStream stream = new FileInputStream(this.ifile);
        final DataInputStream data = new DataInputStream(stream);
        if (!data.readUTF().equals(Backlog.START_MARKER)) {
            throw new IllegalArgumentException("wrong file format");
        }
        return new Iterator<Item>() {
            private final transient AtomicReference<Item> item =
                new AtomicReference<Item>();
            private final transient AtomicBoolean eof = new AtomicBoolean();
            @Override
            public boolean hasNext() {
                if (this.item.get() == null) {
                    try {
                        final String value = data.readUTF();
                        if (value.equals(Backlog.EOF_MARKER)) {
                            this.eof.set(true);
                            stream.close();
                        } else {
                            this.item.set(new Item(value, data.readUTF()));
                        }
                    } catch (java.io.IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
                return !this.eof.get();
            }
            @Override
            public Item next() {
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                }
                return this.item.getAndSet(null);
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException("#remove");
            }
        };
    }

    /**
     * Get name of the file we're working with.
     * @return The file name
     * @throws IOException If some I/O problem inside
     */
    protected final File file() throws IOException {
        return this.ifile;
    }

}
