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

import com.netbout.inf.Attribute;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
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
final class Backlog {

    /**
     * Main file.
     */
    private final transient File file;

    /**
     * Public ctor.
     * @param bck The back log
     * @throws IOException If some I/O problem inside
     */
    public Backlog(final File bck) throws IOException {
        this.file = bck;
        FileUtils.touch(this.file);
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
         * Reference.
         */
        private final transient String ref;
        /**
         * Public ctor.
         * @param value Value
         * @param refr Reference
         */
        public Item(final String value, final String refr) {
            this.val = value;
            this.ref = refr;
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
        public String reference() {
            return this.ref;
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
    public void add(final Item item) throws IOException {
        final OutputStream stream = new FileOutputStream(this.file, true);
        try {
            final DataOutputStream data = new DataOutputStream(stream);
            data.writeUTF(item.value());
            data.writeUTF(item.reference());
            data.flush();
        } finally {
            stream.close();
        }
    }

    /**
     * Get them all.
     *
     * <p>The method is thread-safe.
     *
     * @return The items in iterator
     * @throws IOException If some I/O problem inside
     */
    public Iterator<Item> iterator() throws IOException {
        final InputStream stream = new FileInputStream(this.file);
        final DataInputStream data = new DataInputStream(stream);
        return new Iterator<Item>() {
            @Override
            public boolean hasNext() {
                boolean has;
                try {
                    has = data.available() > 0;
                } catch (java.io.IOException ex) {
                    throw new IllegalStateException(ex);
                }
                if (!has) {
                    IOUtils.closeQuietly(stream);
                }
                return has;
            }
            @Override
            public Item next() {
                try {
                    return new Item(data.readUTF(), data.readUTF());
                } catch (java.io.IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException("#remove");
            }
        };
    }

}
