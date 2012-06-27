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
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import org.apache.commons.io.FilenameUtils;

/**
 * Catalog in a directory.
 *
 * <p>Class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class Catalog {

    /**
     * Main file.
     */
    private final transient File file;

    /**
     * File with duplicates.
     */
    private final transient File dups;

    /**
     * Public ctor.
     * @param file The file to use
     * @throws IOException If some I/O problem inside
     */
    public Catalog(final File ctlg) throws IOException {
        this.file = ctlg;
        this.dups = new File(
            this.file.getParentFile(),
            String.format(
                "%s-dups.%s",
                FilenameUtils.getBaseName(this.file.getPath()),
                FilenameUtils.getExtension(this.file.getPath())
            )
        );
    }

    /**
     * One item.
     *
     * <p>The class is immutable and thread-safe;
     */
    public static final class Item {
        /**
         * Text.
         */
        private final transient String text;
        /**
         * Position.
         */
        private final transient long position;
        /**
         * Public ctor.
         * @param txt The text
         * @param pos The position
         */
        public Item(final String txt, final long pos) {
            this.text = txt;
            this.position = pos;
        }
    }

    /**
     * Get position of numbers in data file, for the given value, or ZERO
     * if such a value is not found in catalog.
     * @param value The value
     * @return Position in data file
     */
    public long seek(final String value) throws IOException {
        // final int target = value.hashCode();
        // final RandomAccessFile data = new RandomAccessFile(this.file, "r");
        // Range range = new Range(0, data.length() / Range.SIZE);
        // while (true) {
        //     final int hash = data.readInt();
        //     if (hash == target) {
        //         next = data.readLong();
        //         break;
        //     }
        //     range = range.move(
        //         data.getFilePointer() / Range.SIZE,
        //         hash,
        //         target
        //     );
        //     if (range) {
        //         break;
        //     }
        //     data.seek(next);
        // }
        return 0;
    }

    /**
     * Create it from scratch, using the provided items.
     * @param items The items to use
     * @param position The position to register
     * @throws IOException If some I/O problem inside
     */
    public void create(final Iterator<Item> items) throws IOException {
        final OutputStream stream = new FileOutputStream(this.file);
        try {
            final DataOutputStream data = new DataOutputStream(stream);
            while (items.hasNext()) {
                final Item item = items.next();
                data.writeInt(item.text.hashCode());
                data.writeLong(item.position);
            }
            data.flush();
        } finally {
            stream.close();
        }
    }

}
