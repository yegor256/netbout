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
 * this code accidentally and without intent to use it, please report this
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
package com.netbout.inf;

import com.jcabi.log.Logger;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import org.apache.commons.io.FileUtils;

/**
 * NFS mounted directory.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 */
final class NfsFolder implements Folder {

    /**
     * Name of master marker.
     */
    private static final String MASTER = "master.txt";

    /**
     * Name of a yield request marker.
     */
    private static final String YIELD = "yield.txt";

    /**
     * The directory.
     */
    private final transient File directory;

    /**
     * Public ctor.
     * @param path Directory, where to mount locally
     * @throws IOException If some error inside
     */
    public NfsFolder(@NotNull final File path) throws IOException {
        this.directory = path;
        this.obtain();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        new File(this.directory, NfsFolder.MASTER).delete();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWritable() throws IOException {
        final File yield = new File(this.directory, NfsFolder.YIELD);
        if (yield.exists()) {
            Logger.info(
                this,
                "#isWritable(): yield request '%s'",
                FileUtils.readFileToString(yield)
            );
        }
        return !yield.exists();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "NfsFolder:%s",
            this.directory.getAbsolutePath()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File path() throws IOException {
        return this.directory;
    }

    /**
     * Make sure the directory belongs to us.
     * @throws IOException If some error inside
     */
    private void obtain() throws IOException {
        final File master = new File(this.directory, NfsFolder.MASTER);
        final File yield = new File(this.directory, NfsFolder.YIELD);
        FileUtils.writeStringToFile(yield, NfsFolder.marker());
        long changed = System.currentTimeMillis();
        String before = "";
        int cycle = 0;
        while (master.exists()) {
            String marker;
            try {
                marker = FileUtils.readFileToString(master);
            } catch (java.io.FileNotFoundException ex) {
                break;
            }
            this.sleep(++cycle, marker);
            if (!marker.equals(before)) {
                before = marker;
                changed = System.currentTimeMillis();
            }
            // @checkstyle MagicNumber (1 line)
            if (System.currentTimeMillis() - changed > 15 * 1000) {
                Logger.info(
                    this,
                    "#obtain(): abandoned folder %s (over %[ms]s)",
                    this.directory,
                    System.currentTimeMillis() - changed
                );
                break;
            }
        }
        FileUtils.writeStringToFile(master, NfsFolder.marker());
        yield.delete();
    }

    /**
     * Marker to write to yield and master files.
     * @return The text
     * @throws IOException If some error inside
     */
    private static String marker() throws IOException {
        return String.format(
            "on %tc by %s/%s",
            new Date(),
            InetAddress.getLocalHost().getHostAddress(),
            ManagementFactory.getRuntimeMXBean().getName()
        );
    }

    /**
     * Sleep at certain waiting cycle.
     * @param cycle Cycle of waiting
     * @param marker The value of the marker we're waiting for
     */
    private void sleep(final int cycle, final String marker) {
        try {
            // @checkstyle MagicNumber (1 line)
            TimeUnit.MILLISECONDS.sleep(cycle * 250);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
        Logger.info(
            this,
            "#sleep(#%d): waiting for '%s' to yield at %s",
            cycle,
            marker,
            this.directory
        );
    }

}
