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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * Default implementation of {@link Directory}.
 *
 * <p>Class is mutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
final class DefaultDirectory implements Directory {

    /**
     * Locked directory.
     */
    private final transient Lock lock;

    /**
     * All versions in the directory.
     */
    private final transient Versions versions;

    /**
     * Directory with baselined files.
     */
    private final transient AtomicReference<Baseline> base =
        new AtomicReference<Baseline>();

    /**
     * Directory with draft.
     */
    private final transient AtomicReference<Draft> draft =
        new AtomicReference<Draft>();

    /**
     * Public ctor.
     * @param file The directory
     * @throws IOException If some I/O problem inside
     */
    public DefaultDirectory(final File file) throws IOException {
        this.lock = new Lock(file);
        this.versions = new Versions(this.lock.dir());
        this.base.set(
            new Baseline(
                new Lock(new File(this.lock.dir(), this.versions.baselined()))
            )
        );
        this.draft.set(
            new Draft(
                new Lock(new File(this.lock.dir(), this.versions.draft()))
            )
        );
        Logger.debug(
            this,
            "#DefaultDirectory('/%s'): started",
            FilenameUtils.getName(file.getPath())
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        try {
            text.append("version: ")
                .append(this.versions.baselined())
                .append(", ")
                .append(FileUtils.sizeOfDirectory(this.lock.dir()))
                .append(" bytes");
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
        return text.toString();
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
        final long pos = this.base.get().catalog(attr).seek(value);
        if (pos >= 0) {
            final File file = this.base.get().data(attr);
            final RandomAccessFile data = new RandomAccessFile(file, "r");
            try {
                data.seek(pos);
                final InputStream istream =
                    Channels.newInputStream(data.getChannel());
                try {
                    nums.load(istream);
                } finally {
                    istream.close();
                }
                Logger.debug(
                    this,
                    // @checkstyle LineLength (1 line)
                    "#load('%s', '%[text]s', ..): loaded numbers from pos #%d (file.length=%s, file.name=/%s)",
                    attr,
                    value,
                    pos,
                    FileUtils.byteCountToDisplaySize(file.length()),
                    FilenameUtils.getName(file.getPath())
                );
            } finally {
                data.close();
            }
        } else {
            nums.load(DefaultDirectory.emptyStream());
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
        InputStream stream;
        if (file.length() > 0) {
            stream = new FileInputStream(file);
        } else {
            stream = DefaultDirectory.emptyStream();
        }
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
        final String version = this.versions.draft();
        final Baseline candidate = new Baseline(
            new Lock(new File(this.lock.dir(), version))
        );
        this.draft.get().baseline(candidate, this.base.get());
        this.base.get().close();
        this.base.get().expire();
        this.base.set(candidate);
        this.draft.get().close();
        this.draft.get().expire();
        this.draft.set(
            new Draft(
                new Lock(new File(this.lock.dir(), this.versions.draft()))
            )
        );
        this.versions.rebase(version);
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

    /**
     * Create input stream with no numbers (just leading ZERO long).
     * @return Stream
     * @throws IOException If some I/O problem inside
     */
    private static InputStream emptyStream() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DataOutputStream data = new DataOutputStream(baos);
        data.writeLong(0L);
        final byte[] bytes = baos.toByteArray();
        return new ByteArrayInputStream(bytes);
    }

}
