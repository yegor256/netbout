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

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.ymock.util.Logger;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
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
     * Mounting directory.
     */
    private final transient File directory;

    /**
     * Our host.
     */
    private final transient String host;

    /**
     * Public ctor.
     * @param path Directory
     */
    public EbsDirectory(final File path) {
        this(path, "localhost");
    }

    /**
     * Public ctor.
     * @param path Directory
     * @param hst The host where we're working
     */
    public EbsDirectory(final File path, final String hst) {
        this.directory = path;
        this.host = hst;
    }

    /**
     * The path.
     * @return File
     */
    public File path() {
        if (!this.directory.exists()) {
            if (this.directory.mkdirs()) {
                Logger.info(
                    this,
                    "#path(): directory '%s' created",
                    this.directory
                );
            } else {
                Logger.info(
                    this,
                    "#path(): directory '%s' already exists",
                    this.directory
                );
            }
        }
        return this.directory;
    }

    /**
     * The directory is mounted already?
     * @return Yes or no?
     * @throws IOException If some IO problem inside
     */
    public boolean mounted() throws IOException {
        // @checkstyle MultipleStringLiterals (1 line)
        final String output = this.exec("mount");
        final boolean mounted = output.contains(this.path().getPath());
        if (mounted) {
            Logger.info(
                this,
                "#mounted(): '%s' is already mounted:\n%s",
                this.path(),
                output
            );
        } else {
            Logger.info(
                this,
                "#mounted(): '%s' is not mounted yet:\n%s",
                this.path(),
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
        final String name = device.name();
        final String output = this.exec(
            "sudo",
            "mount",
            name,
            this.path()
        );
        Logger.info(
            this,
            "#mount(%s): mounted as %s:\n%s",
            name,
            this.path(),
            output
        );
    }

    /**
     * Execute unix command and return it's output as a string.
     * @param args Arguments of shell command
     * @return The output as a string (trimmed)
     * @throws IOException If some IO problem inside
     */
    private String exec(final Object... args) throws IOException {
        try {
            final Session session = this.session();
            session.connect();
            final ChannelExec exec = (ChannelExec) session.openChannel("exec");
            final String command = this.command(args);
            exec.setCommand(command);
            exec.connect();
            final String output = IOUtils.toString(exec.getInputStream());
            final int code = this.code(exec);
            if (code != 0) {
                throw new IllegalStateException(
                    String.format(
                        "Failed to execute \"%s\" (code=%d):\n%s\n%s",
                        command,
                        code,
                        output,
                        IOUtils.toString(exec.getErrStream())
                    )
                );
            }
            exec.disconnect();
            session.disconnect();
            return output;
        } catch (com.jcraft.jsch.JSchException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Wait until it's done and return its code.
     * @param exec The channel
     * @return The exit code
     * @throws IOException If some IO problem inside
     */
    private int code(final ChannelExec exec) throws IOException {
        int retry = 0;
        while (!exec.isClosed()) {
            ++retry;
            try {
                TimeUnit.SECONDS.sleep(retry * 1L);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            }
            // @checkstyle MagicNumber (1 line)
            if (retry > 10) {
                break;
            }
            Logger.info(this, "#pause(..): waiting for SSH to close...");
        }
        return exec.getExitStatus();
    }

    /**
     * Create and return a session.
     * @return JSch session
     * @throws IOException If some IO problem inside
     */
    private Session session() throws IOException {
        try {
            final JSch jsch = new JSch();
            jsch.setConfig("StrictHostKeyChecking", "no");
            jsch.setLogger(
                new com.jcraft.jsch.Logger() {
                    @Override
                    public boolean isEnabled(final int level) {
                        return true;
                    }
                    @Override
                    public void log(final int level, final String msg) {
                        Logger.info(EbsDirectory.class, "%s", msg);
                    }
                }
            );
            jsch.addIdentity(this.key().getPath());
            return jsch.getSession("ec2-user", this.host);
        } catch (com.jcraft.jsch.JSchException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Create command.
     * @param args Arguments of shell command
     * @return The command
     */
    private String command(final Object... args) {
        final StringBuilder command = new StringBuilder();
        for (Object arg : args) {
            command.append("'")
                .append(arg.toString())
                .append("' ");
        }
        return command.toString();
    }

    /**
     * Get file with secret key.
     * @return The file
     * @throws IOException If some IO problem inside
     */
    private File key() throws IOException {
        final File file = File.createTempFile("netbout-ebs", ".pem");
        final URL key = this.getClass().getResource("ebs.pem");
        if (key == null) {
            throw new IOException("PEM not found");
        }
        FileUtils.copyURLToFile(key, file);
        return file;
    }

}
