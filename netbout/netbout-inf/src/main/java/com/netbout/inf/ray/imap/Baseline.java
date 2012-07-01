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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;

/**
 * Sub-directory with baselined documents.
 *
 * <p>Class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class Baseline implements Closeable {

    /**
     * Lock on the directory.
     */
    private final transient Lock lock;

    /**
     * Public ctor.
     * @param lck The directory where to work
     * @throws IOException If some I/O problem inside
     */
    public Baseline(final Lock lck) throws IOException {
        this.lock = lck;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("base:%s", this.lock.toString());
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
    public boolean equals(final Object base) {
        return this == base || (base instanceof Baseline
            && Baseline.class.cast(base).lock.equals(this.lock));
    }

    /**
     * Expire it.
     * @throws IOException If some I/O problem inside
     */
    public void expire() throws IOException {
        this.lock.expire();
    }

    /**
     * Get name of data file.
     * @param attr Attribute
     * @return File name
     * @throws IOException If some I/O problem inside
     */
    public File data(final Attribute attr) throws IOException {
        final File file = new File(
            this.lock.dir(),
            String.format("/%s/data.inf", attr)
        );
        FileUtils.touch(file);
        return file;
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
     * Get catalog.
     * @param attr Attribute
     * @return The catalog
     */
    public Catalog catalog(final Attribute attr) throws IOException {
        return new Catalog(
            new File(
                this.lock.dir(),
                String.format("/%s/catalog.inf", attr)
            )
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

}
