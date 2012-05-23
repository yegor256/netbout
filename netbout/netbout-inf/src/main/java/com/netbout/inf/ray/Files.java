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
package com.netbout.inf.ray;

import com.jcabi.log.Logger;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.apache.commons.io.FileUtils;

/**
 * Files for the map.
 *
 * <p>This class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class Files {

    /**
     * Name of marker file (with version inside).
     */
    public static final String MARKER = "version.txt";

    /**
     * The directory.
     */
    private final transient File dir;

    /**
     * Public ctor.
     * @param file Directory where files are kept
     * @throws IOException If some IO error
     */
    public Files(final File file) throws IOException {
        this.dir = file;
        this.dir.mkdirs();
    }

    /**
     * Get snapshot to read.
     * @return The snapshot where we can read from
     * @throws IOException If some IO error
     */
    public Snapshot reader() throws IOException {
        final File marker = new File(this.dir, Files.MARKER);
        Snapshot snapshot;
        if (marker.exists()) {
            snapshot = new Snapshot(
                this.dir,
                FileUtils.readFileToString(marker)
            );
        } else {
            snapshot = this.writer();
        }
        return snapshot;
    }

    /**
     * Get snapshot to write.
     * @return The snapshot where we can write to
     * @throws IOException If some IO error
     */
    public Snapshot writer() throws IOException {
        return new Snapshot(
            this.dir,
            String.format(
                "%06X",
                // @checkstyle MagicNumber (1 line)
                UUID.randomUUID().getMostSignificantBits() & 0xFFFFFF
            )
        );
    }

    /**
     * Make this snapshot the main one.
     * @param snapshot The snapshot to publish
     * @throws IOException If some IO error
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void publish(final Snapshot snapshot) throws IOException {
        final long start = System.currentTimeMillis();
        FileUtils.writeStringToFile(
            new File(this.dir, Files.MARKER),
            snapshot.version()
        );
        for (String name : this.dir.list()) {
            if (!name.equals(Files.MARKER) && !snapshot.includes(name)) {
                FileUtils.deleteQuietly(new File(this.dir, name));
            }
        }
        Logger.info(
            this,
            "#publish('%s'): done in %[ms]s",
            snapshot,
            System.currentTimeMillis() - start
        );
    }

}
