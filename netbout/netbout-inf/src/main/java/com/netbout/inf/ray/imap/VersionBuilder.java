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
import java.util.UUID;
import org.apache.commons.io.FileUtils;

/**
 * Builder of version.
 *
 * <p>Class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class VersionBuilder {

    /**
     * Version file name.
     */
    private static final String VFILE = "version.txt";

    /**
     * Directory.
     */
    private final transient File dir;

    /**
     * Public ctor.
     * @param file The directory
     * @throws IOException If some I/O problem inside
     */
    public VersionBuilder(final File file) throws IOException {
        this.dir = file;
    }

    /**
     * Set new baselined version.
     * @param version New version
     * @throws IOException If some I/O problem inside
     */
    public void rebase(final String version) throws IOException {
        final String previous = FileUtils.readFileToString(
            new File(this.dir, VersionBuilder.VFILE)
        );
        if (version.equals(previous)) {
            throw new IllegalArgumentException("same version in rebase");
        }
        FileUtils.writeStringToFile(
            new File(this.dir, VersionBuilder.VFILE),
            version
        );
        Logger.debug(
            this,
            "#rebase('%s'): rebased from %s",
            version,
            previous
        );
    }

    /**
     * Get currently baselined version (or create one if it doesn't exist).
     * @return Version
     * @throws IOException If some I/O problem inside
     */
    public String baselined() throws IOException {
        final File marker = new File(this.dir, VersionBuilder.VFILE);
        if (!marker.exists()) {
            FileUtils.writeStringToFile(marker, this.ver());
        }
        return FileUtils.readFileToString(marker);
    }

    /**
     * Generate draft version.
     * @return Version
     * @throws IOException If some I/O problem inside
     */
    public String draft() throws IOException {
        return this.ver();
    }

    /**
     * Generate version.
     * @return Version
     */
    private String ver() {
        String ver;
        do {
            ver = String.format(
                "%06X",
                // @checkstyle MagicNumber (1 line)
                UUID.randomUUID().getMostSignificantBits() & 0xFFFFFF
            );
        } while (new File(this.dir, ver).exists());
        return ver;
    }

}
