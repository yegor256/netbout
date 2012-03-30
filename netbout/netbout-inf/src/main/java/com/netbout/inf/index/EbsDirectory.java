/**
 * Copyright (c) 2009-2011, netBout.com
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
package com.netbout.inf.index;

import com.ymock.util.Logger;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Directory for EBS volume.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 */
final class EbsDirectory {

    /**
     * Mounter.
     */
    private static final File MOUNTER = EbsDirectory.bin("mount");

    /**
     * Mounting directory.
     */
    private final transient File directory;

    /**
     * Public ctor.
     * @param path Directory
     */
    public EbsDirectory(final File path) {
        this.directory = path;
    }

    /**
     * The path.
     * @return File
     */
    public File path() {
        return this.directory;
    }

    /**
     * The directory is mounted already?
     * @return Yes or no?
     * @throws IOException If some IO problem inside
     */
    public boolean mounted() throws IOException {
        final String output = this.exec(
            new ProcessBuilder(EbsDirectory.MOUNTER.getPath())
        );
        final boolean mounted = output.contains(this.directory.getPath());
        if (mounted) {
            Logger.info(
                this,
                "#mounted(): '%s' is already mounted:\n%s",
                this.directory,
                output
            );
        } else {
            Logger.info(
                this,
                "#mounted(): '%s' is not mounted yet:\n%s",
                this.directory,
                output
            );
        }
        return mounted;
    }

    /**
     * Mount this device to our directory.
     * @param device Name of device to mount
     * @throws IOException If some IO problem inside
     */
    public void mount(final EbsDevice device) throws IOException {
        FileUtils.deleteQuietly(this.directory);
        this.directory.mkdirs();
        final String name = device.name();
        final String output = this.exec(
            new ProcessBuilder(
                EbsDirectory.MOUNTER.getPath(),
                name,
                this.directory.getPath()
            )
        );
        Logger.info(
            this,
            "#mount(%s): mounted as %s:\n%s",
            name,
            this.directory,
            output
        );
    }

    /**
     * Execute unix command and return it's output as a string.
     * @param bldr The process builder to use
     * @return The output as a string (trimmed)
     * @throws IOException If some IO problem inside
     */
    private String exec(final ProcessBuilder bldr) throws IOException {
        final Process proc = bldr.start();
        int code;
        try {
            code = proc.waitFor();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException(ex);
        }
        if (code != 0) {
            throw new IOException(
                String.format(
                    "Abnormal termination: %s",
                    IOUtils.toString(proc.getErrorStream())
                )
            );
        }
        return IOUtils.toString(proc.getInputStream()).trim();
    }

    /**
     * Find binary and return its full name.
     * @param name Short name
     * @return Full name
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private static File bin(final String name) {
        final String[] paths = new String[] {
            "/bin",
            "/usr/bin",
            "/sbin",
            "/usr/sbin",
        };
        File file = null;
        for (String path : paths) {
            final File bin = new File(path, name);
            if (bin.exists() && bin.isFile()) {
                file = bin;
                break;
            }
        }
        if (file == null) {
            throw new IllegalStateException(
                String.format(
                    "Failed to find executable of '%s'",
                    name
                )
            );
        }
        return file;
    }

}
