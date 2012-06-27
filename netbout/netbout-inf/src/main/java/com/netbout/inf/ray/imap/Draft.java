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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;

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
     * Directory.
     */
    private final transient File dir;

    /**
     * Version to work with.
     */
    private final transient String version;

    /**
     * Public ctor.
     * @param file The directory
     * @throws IOException If some I/O problem inside
     */
    public Draft(final File file) throws IOException {
        this.dir = file;
        this.version = new VersionBuilder(this.dir).draft();
    }

    /**
     * Get name of numbers file.
     * @param attr Attribute
     * @param value The value
     * @return File name
     * @throws IOException If some I/O problem inside
     */
    public File numbers(final Attribute attr,
        final String value) throws IOException {
        return new File(
            this.dir,
            String.format(
                "/%s/%s/numbers-%d.inf",
                this.version,
                attr,
                value.hashCode()
            )
        );
    }

    /**
     * Get name of reverse file.
     * @param attr Attribute
     * @return File name
     * @throws IOException If some I/O problem inside
     */
    public File reverse(final Attribute attr) throws IOException {
        return new File(
            this.dir,
            String.format("/%s/%s/reverse.inf", this.version, attr)
        );
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
                this.dir,
                String.format("/%s/%s/backlog.inf", this.version, attr)
            )
        );
    }

    /**
     * Baseline to the given place.
     * @param base Baseline to baseline to
     * @throws IOException If some I/O problem inside
     */
    public void baseline(final Baseline base) throws IOException {
        for (File file : this.dir.listFiles()) {
            if (!file.isDirectory()) {
                continue;
            }
            final Attribute attr = new Attribute(
                FilenameUtils.getName(file.getPath())
            );
            this.baseline(base, attr);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        // todo
    }

    /**
     * Baseline to the given place, for the given attribute.
     * @param base Baseline to baseline to
     * @param attr Attribute
     * @throws IOException If some I/O problem inside
     */
    private void baseline(final Baseline base,
        final Attribute attr) throws IOException {
        final File reverse = this.reverse(attr);
        if (reverse.exists()) {
            FileUtils.copyFile(reverse, base.reverse(attr));
        }
    }

}
