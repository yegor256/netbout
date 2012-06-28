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
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.io.FilenameUtils;

/**
 * Default implementation of {@link Directory}.
 *
 * <p>Class is mutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class DefaultDirectory implements Directory {

    /**
     * Locked directory.
     */
    private final transient Lock lock;

    /**
     * Directory with baselined files.
     */
    private final transient AtomicReference<Baseline> base =
        new AtomicReference<Baseline>();

    /**
     * Directory with draft.
     */
    private transient AtomicReference<Draft> draft =
        new AtomicReference<Draft>();

    /**
     * Public ctor.
     * @param file The directory
     */
    public DefaultDirectory(final File file) throws IOException {
        this.lock = new Lock(file);
        this.base.set(new Baseline(this.lock.dir()));
        this.draft.set(new Draft(this.lock.dir()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(final Attribute attr, final String value,
        final Numbers nums) throws IOException {
        final File file = this.draft.get().numbers(attr);
        final OutputStream stream = new FileOutputStream(file);
        try {
            nums.save(stream);
        } finally {
            stream.close();
        }
        this.draft.get().backlog(attr).add(
            new Backlog.Item(
                value,
                FilenameUtils.getName(file.getPath())
            )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(final Attribute attr, final String value,
        final Numbers nums) throws IOException {
        final File file = this.base.get().data(attr);
        final RandomAccessFile data = new RandomAccessFile(file, "r");
        try {
            final long pos = this.base.get().catalog(attr).seek(value);
            if (pos >= 0) {
                data.seek(pos);
                final InputStream istream =
                    Channels.newInputStream(data.getChannel());
                try {
                    nums.load(istream);
                } finally {
                    istream.close();
                }
            } else {
                nums.load(new ByteArrayInputStream(new byte[0]));
            }
        } finally {
            data.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(final Attribute attr,
        final Reverse reverse) throws IOException {
        final File file = this.draft.get().reverse(attr);
        final OutputStream stream = new FileOutputStream(file);
        try {
            reverse.save(stream);
        } finally {
            stream.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(final Attribute attr,
        final Reverse reverse) throws IOException {
        final File file = this.base.get().reverse(attr);
        final InputStream stream = new FileInputStream(file);
        try {
            reverse.load(stream);
        } finally {
            stream.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void baseline() throws IOException {
        final Baseline candidate = new Baseline(
            this.lock.dir(),
            new VersionBuilder(this.lock.dir()).draft()
        );
        this.draft.get().baseline(candidate, this.base.get());
        this.base.get().close();
        this.base.set(candidate);
        this.draft.set(new Draft(this.lock.dir()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.base.get().close();
        this.base.set(null);
        this.draft.get().close();
        this.draft.set(null);
        this.lock.close();
    }

}
