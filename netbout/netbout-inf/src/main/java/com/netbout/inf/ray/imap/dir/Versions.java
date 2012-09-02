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
package com.netbout.inf.ray.imap.dir;

import com.jcabi.log.Logger;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * Versions in directory.
 *
 * <p>Class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class Versions {

    /**
     * Name of main version marker.
     */
    private static final String MARKER = "version.txt";

    /**
     * Directory.
     */
    private final transient File dir;

    /**
     * Public ctor.
     * @param file The directory
     * @throws IOException If some I/O problem inside
     */
    public Versions(final File file) throws IOException {
        this.dir = file;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        text.append(this.dir.getPath());
        for (File folder : this.dir.listFiles()) {
            if (!folder.isDirectory()) {
                continue;
            }
            text.append(" ")
                .append(FilenameUtils.getName(folder.getPath()))
                .append(":")
                .append(
                    FileUtils.byteCountToDisplaySize(
                        FileUtils.sizeOfDirectory(folder)
                    )
                );
        }
        return text.toString();
    }

    /**
     * Set new baselined version.
     * @param version New version
     * @throws IOException If some I/O problem inside
     */
    public void rebase(final String version) throws IOException {
        final String previous = FileUtils.readFileToString(
            new File(this.dir, Versions.MARKER)
        );
        if (version.equals(previous)) {
            throw new IllegalArgumentException("same version in rebase");
        }
        FileUtils.writeStringToFile(
            new File(this.dir, Versions.MARKER),
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
        final File marker = new File(this.dir, Versions.MARKER);
        if (!marker.exists()) {
            FileUtils.writeStringToFile(marker, this.fresh());
        }
        String version = FileUtils.readFileToString(marker);
        final File folder = new File(this.dir, version);
        if (folder.exists()) {
            Logger.debug(
                this,
                "#baselined(): restored version '%s'",
                version
            );
        } else {
            Logger.warn(
                this,
                "#baselined(): broken version '%s'",
                version
            );
            version = this.fresh();
            FileUtils.writeStringToFile(marker, version);
        }
        return version;
    }

    /**
     * Generate draft version.
     * @return Version
     * @throws IOException If some I/O problem inside
     */
    public String draft() throws IOException {
        return this.fresh();
    }

    /**
     * Remove old versions from disc (except the provided draft version).
     * @throws IOException If some I/O problem inside
     */
    public void clear() throws IOException {
        final String base = this.baselined();
        for (File folder : this.dir.listFiles()) {
            if (!folder.isDirectory()) {
                continue;
            }
            final String name = FilenameUtils.getName(folder.getPath());
            if (name.equals(base)) {
                continue;
            }
            FileUtils.deleteDirectory(folder);
            Logger.info(this, "#clear(): deleted %s", folder);
        }
    }

    /**
     * Generate fresh version.
     * @return Version
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private String fresh() {
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
