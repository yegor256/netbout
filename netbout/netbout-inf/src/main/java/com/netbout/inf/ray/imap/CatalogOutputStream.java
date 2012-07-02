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

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Output stream with {@link Catalog} items.
 *
 * <p>The class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class CatalogOutputStream implements Closeable {

    /**
     * The stream to write to.
     */
    private final transient DataOutputStream output;

    /**
     * Most recently added item, buffered.
     */
    private final transient AtomicReference<Catalog.Item> buffer =
        new AtomicReference<Catalog.Item>();

    /**
     * Public ctor.
     * @param file The file to use
     * @throws IOException If some I/O problem inside
     */
    public CatalogOutputStream(final File file) throws IOException {
        this.output = new DataOutputStream(new FileOutputStream(file));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.flush();
        this.output.close();
    }

    /**
     * Add new item.
     * @param item The items to add
     * @throws IOException If some I/O problem inside
     */
    public void write(final Catalog.Item item) throws IOException {
        this.flush();
        this.buffer.set(item);
    }

    /**
     * Make one step back.
     * @throws IOException If some I/O problem inside
     */
    public void back() throws IOException {
        this.buffer.set(null);
    }

    /**
     * Flush buffer to stream.
     * @throws IOException If some I/O problem inside
     */
    private void flush() throws IOException {
        final Catalog.Item item = this.buffer.getAndSet(null);
        if (item != null) {
            this.output.writeInt(item.hashCode());
            this.output.writeLong(item.position());
        }
    }

}
