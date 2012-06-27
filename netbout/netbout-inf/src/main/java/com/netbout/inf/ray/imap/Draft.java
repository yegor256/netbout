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
    private final transient String version = "1";

    /**
     * Public ctor.
     * @param file The directory
     */
    public Draft(final File file) throws IOException {
        this.dir = file;
    }

    /**
     * Get name of numbers file.
     * @param attr Attribute
     * @param value The value
     * @return File name
     */
    public File numbers(final Attribute attr,
        final String value) throws IOException {
        return null;
    }

    /**
     * Get name of reverse file.
     * @param attr Attribute
     * @return File name
     */
    public File reverse(final Attribute attr) throws IOException {
        return null;
    }

    /**
     * Get backlog.
     * @param attr Attribute
     * @return The catalog
     */
    public Draft.Backlog backlog(final Attribute attr) throws IOException {
        return null;
    }

    /**
     * Baseline to the given place.
     * @param base Baseline to baseline to
     */
    public void baseline(final Baseline base) throws IOException {
        // todo
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        // todo
    }

    /**
     * Backlog.
     *
     * <p>Class is thread-safe.
     */
    public final class Backlog {
        /**
         * Register new file for the value.
         * @param value The value
         * @param file File with numbers
         */
        public void register(final String value,
            final File file) throws IOException {
            // todo
        }
    }

}
