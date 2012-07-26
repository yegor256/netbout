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
package com.netbout.inf.ray.imap.dir;

import com.jcabi.log.Logger;
import com.netbout.inf.Attribute;
import com.netbout.inf.Stash;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.io.FileUtils;

/**
 * Sub-directory with baselined documents.
 *
 * <p>Class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class Baseline extends AbstractVersion {

    /**
     * Stash.
     */
    private final transient Stash istash;

    /**
     * Public ctor.
     * @param lock The directory where to work
     * @throws IOException If some I/O problem inside
     */
    public Baseline(final Lock lock) throws IOException {
        super(lock);
        this.istash = new DefaultStash(new File(this.dir(), "_stash"));
        final AtomicInteger failures = new AtomicInteger();
        try {
            new CompositeAuditor().audit(
                this,
                new Audit() {
                    @Override
                    public void problem(final String text) {
                        // @checkstyle MagicNumber (1 line)
                        if (failures.getAndIncrement() < 10) {
                            Logger.warn(this, "audit: %s", text);
                        }
                    }
                }
            );
        } catch (IOException ex) {
            failures.getAndIncrement();
            Logger.warn(this, "audit failed: %[exception]s", ex);
        }
        if (failures.get() > 0) {
            Logger.warn(
                this,
                "cleaning baseline, %d failures in audit",
                failures.get()
            );
            lock.clear();
        }
    }

    /**
     * Get name of data file.
     * @param attr Attribute
     * @return File name
     * @throws IOException If some I/O problem inside
     */
    public File data(final Attribute attr) throws IOException {
        final File file = new File(
            this.dir(),
            String.format("/%s/data.inf", attr)
        );
        FileUtils.touch(file);
        return file;
    }

    /**
     * Get catalog.
     * @param attr Attribute
     * @return The catalog
     * @throws IOException If some I/O problem inside
     */
    public Catalog catalog(final Attribute attr) throws IOException {
        return new Catalog(
            new File(
                this.dir(),
                String.format("/%s/catalog.inf", attr)
            )
        );
    }

    /**
     * Get stash.
     * @return The stash
     * @throws IOException If some I/O problem inside
     */
    public Stash stash() throws IOException {
        return this.istash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void closeDeps() throws IOException {
        this.istash.close();
    }

}
