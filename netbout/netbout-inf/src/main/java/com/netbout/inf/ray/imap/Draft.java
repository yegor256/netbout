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
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Sub-directory with draft documents.
 *
 * <p>Class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class Draft implements Closeable {

    /**
     * Lock on the directory.
     */
    private final transient Lock lock;

    /**
     * Public ctor.
     * @param file The directory
     * @throws IOException If some I/O problem inside
     */
    public Draft(final File file) throws IOException {
        final String ver = new VersionBuilder(file).draft();
        this.lock = new Lock(
            new File(
                file,
                String.format("/%s", ver)
            )
        );
        Logger.debug(
            this,
            "#Draft(/%s): started %s",
            FilenameUtils.getName(file.getPath()),
            ver
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("draft:%s", this.lock.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.lock.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object draft) {
        return this == draft || (draft instanceof Draft
            && Draft.class.cast(draft).lock.equals(this.lock));
    }

    /**
     * Expire it.
     * @throws IOException If some I/O problem inside
     */
    public void expire() throws IOException {
        this.lock.expire();
    }

    /**
     * Create new temp file for numbers.
     * @param attr Attribute
     * @return File name
     * @throws IOException If some I/O problem inside
     */
    public File numbers(final Attribute attr) throws IOException {
        final File folder = new File(this.lock.dir(), attr.toString());
        folder.mkdirs();
        return File.createTempFile("numbers-", ".inf", folder);
    }

    /**
     * Get name of reverse file.
     * @param attr Attribute
     * @return File name
     * @throws IOException If some I/O problem inside
     */
    public File reverse(final Attribute attr) throws IOException {
        final File file = new File(
            this.lock.dir(),
            String.format("/%s/reverse.inf", attr)
        );
        FileUtils.touch(file);
        return file;
    }

    /**
     * Get backlog.
     * @param attr Attribute
     * @return The catalog
     * @throws IOException If some I/O problem inside
     */
    public Backlog backlog(final Attribute attr) throws IOException {
        return new Backlog(
            new File(
                this.lock.dir(),
                String.format("/%s/backlog.inf", attr)
            )
        );
    }

    /**
     * Baseline it to a new place.
     * @param dest Where to save baseline
     * @param src Original baseline
     * @throws IOException If some I/O problem inside
     */
    public void baseline(final Baseline dest,
        final Baseline src) throws IOException {
        final long start = System.currentTimeMillis();
        final Collection<Attribute> attrs = new LinkedList<Attribute>();
        for (File file : this.lock.dir().listFiles()) {
            if (!file.isDirectory()) {
                continue;
            }
            final Attribute attr = new Attribute(
                FilenameUtils.getName(file.getPath())
            );
            this.baseline(dest, src, attr);
            attrs.add(attr);
        }
        Logger.debug(
            this,
            "#baseline('%s', '%s'): baselined %[list]s in %[ms]s",
            dest,
            src,
            attrs,
            System.currentTimeMillis() - start
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.lock.close();
        Logger.debug(this, "#close(): closed");
    }

    /**
     * Baseline to the given place, for the given attribute.
     * @param dest Where to save baseline
     * @param src Original baseline
     * @param attr Attribute
     * @throws IOException If some I/O problem inside
     */
    private void baseline(final Baseline dest, final Baseline src,
        final Attribute attr) throws IOException {
        File reverse = this.reverse(attr);
        if (!reverse.exists()) {
            reverse = src.reverse(attr);
        }
        if (reverse.exists()) {
            FileUtils.copyFile(reverse, dest.reverse(attr));
        }
        final Pipeline pipeline = new Pipeline(dest, src, attr);
        dest.catalog(attr).create(pipeline);
        pipeline.close();
    }

    /**
     * Token.
     */
    interface Token {
        /**
         * Convert it to item.
         * @return The item
         * @throws IOException If some IO problem inside
         */
        Catalog.Item item() throws IOException;
    }

    /**
     * Comparable token.
     */
    interface ComparableToken extends Token, Comparable<Token> {
    }

    /**
     * Pipeline between back log and new catalog.
     *
     * <p>The class is thread-safe.
     */
    private final class Pipeline implements Iterator<Catalog.Item>, Closeable {
        /**
         * Source catalog.
         */
        private final transient Catalog catalog;
        /**
         * The attribute.
         */
        private final transient Attribute attribute;
        /**
         * Backlog with data.
         */
        private final transient Iterator<Backlog.Item> biterator;
        /**
         * Pre-existing catalog with data.
         */
        private final transient Iterator<Catalog.Item> citerator;
        /**
         * Pre-existing data file.
         */
        private final transient RandomAccessFile data;
        /**
         * Output stream.
         */
        private final transient OutputStream output;
        /**
         * Current position in output stream.
         */
        private final transient AtomicLong opos = new AtomicLong();
        /**
         * Information about the item already retrieved from iterators.
         */
        private final transient AtomicReference<Token> token =
            new AtomicReference<Token>();
        /**
         * Public ctor.
         * @param dest Destination
         * @param src Source
         * @param attr Attribute
         * @throws IOException If some IO problem inside
         */
        public Pipeline(final Baseline dest, final Baseline src,
            final Attribute attr) throws IOException {
            this.attribute = attr;
            this.catalog = src.catalog(this.attribute);
            // we should cache values first
            this.biterator = Draft.this.backlog(this.attribute).iterator();
            this.citerator = this.catalog.iterator();
            this.data = new RandomAccessFile(src.data(this.attribute), "r");
            this.output = new FileOutputStream(dest.data(this.attribute));
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void close() throws IOException {
            this.data.close();
            this.output.close();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            return this.biterator.hasNext() || this.citerator.hasNext()
                || this.token.get() != null;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Catalog.Item next() {
            try {
                if (this.token.get() == null) {
                    if (this.citerator.hasNext()) {
                        this.token.set(this.cattoken(this.citerator));
                    } else if (this.biterator.hasNext()) {
                        this.token.set(this.backtoken(this.biterator));
                    } else {
                        throw new NoSuchElementException();
                    }
                }
                Catalog.Item next;
                if (this.token.get() instanceof ComparableToken) {
                    final ComparableToken comparable =
                        ComparableToken.class.cast(this.token.get());
                    final Token tkn = this.backtoken(this.biterator);
                    if (comparable.compareTo(tkn) >= 0) {
                        next = tkn.item();
                    } else {
                        next = comparable.item();
                        this.token.set(tkn);
                    }
                } else {
                    next = this.token.get().item();
                    this.token.set(null);
                }
                return next;
            } catch (java.io.IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("#remove");
        }
        /**
         * Get token from backlog.
         * @param iterator The iterator to read from
         * @return Token retrieved
         */
        private Token backtoken(final Iterator<Backlog.Item> iterator) {
            final Backlog.Item item = iterator.next();
            return new Token() {
                @Override
                public Catalog.Item item() throws IOException {
                    final File file = new File(
                        Draft.this.lock.dir(),
                        String.format(
                            "/%s/%s",
                            Pipeline.this.attribute,
                            item.path()
                        )
                    );
                    final InputStream input = new FileInputStream(file);
                    final int len = IOUtils.copy(input, Pipeline.this.output);
                    input.close();
                    Logger.debug(
                        this,
                        "#item('%s'): copied %d bytes from '/%s'",
                        Pipeline.this.attribute,
                        len,
                        FilenameUtils.getName(file.getPath())
                    );
                    return new Catalog.Item(
                        item.value(),
                        Pipeline.this.opos.getAndAdd(len)
                    );
                }
                @Override
                public int hashCode() {
                    return item.hashCode();
                }
                @Override
                public boolean equals(final Object token) {
                    return this == token || token.hashCode() == this.hashCode();
                }
            };
        }
        /**
         * Get token from catalog.
         * @param iterator The iterator to read from
         * @return Token retrieved
         */
        private Token cattoken(final Iterator<Catalog.Item> iterator) {
            final Catalog.Item item = iterator.next();
            return new ComparableToken() {
                @Override
                public Catalog.Item item() throws IOException {
                    final long pos = Pipeline.this.catalog.seek(item.value());
                    Pipeline.this.data.seek(pos);
                    final InputStream input = Channels.newInputStream(
                        Pipeline.this.data.getChannel()
                    );
                    final int len = IOUtils.copy(input, Pipeline.this.output);
                    input.close();
                    Logger.debug(
                        this,
                        "#item(): copied %d bytes from pos #%d",
                        len,
                        pos
                    );
                    return new Catalog.Item(
                        item.value(),
                        Pipeline.this.opos.getAndAdd(len)
                    );
                }
                @Override
                public int compareTo(final Token token) {
                    return new Integer(this.hashCode()).compareTo(
                        new Integer(token.hashCode())
                    );
                }
                @Override
                public int hashCode() {
                    return item.hashCode();
                }
                @Override
                public boolean equals(final Object token) {
                    return this == token || token.hashCode() == this.hashCode();
                }
            };
        }
    }

}
