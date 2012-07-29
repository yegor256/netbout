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
package com.netbout.inf;

import com.jcabi.log.Logger;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.CharEncoding;

/**
 * NFS mounted directory.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @checkstyle MultipleStringLiterals (500 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
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
     * Mounting directory.
     */
    private final transient File directory;

    /**
     * Public ctor.
     * @param path Directory, where to mount locally
     * @throws IOException If some error inside
     */
    public NfsFolder(final File path) throws IOException {
        this.directory = path;
        if (this.directory.getPath().startsWith("/mnt")) {
            if (!this.mounted()) {
                this.mount();
            }
        } else {
            Logger.info(this, "#path(): mount is not required");
        }
        this.obtain();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        if (this.directory.getPath().startsWith("/mnt")) {
            this.exec("umount", this.directory);
        } else {
            Logger.info(this, "#close(): no umount required");
        }
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
        final StringBuilder text = new StringBuilder();
        text.append(
            String.format(
                "directory: %s\n",
                this.directory.getAbsolutePath()
            )
        );
        try {
            text.append(this.exec("mount"));
        } catch (IOException ex) {
            text.append(Logger.format("%[exception]s", ex));
        }
        return text.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File path() throws IOException {
        return this.directory;
    }

    /**
     * The directory is mounted already?
     * @return Yes or no?
     * @throws IOException If some IO problem inside
     */
    private boolean mounted() throws IOException {
        final String output = this.exec("mount");
        final boolean mounted = output.contains(this.directory.getPath());
        if (mounted) {
            Logger.info(
                this,
                "#mounted(): '%s' is already mounted",
                this.directory
            );
        } else {
            Logger.info(
                this,
                "#mounted(): '%s' is not mounted yet",
                this.directory
            );
        }
        return mounted;
    }

    /**
     * Mount this device to our directory.
     * @throws IOException If some IO problem inside
     * @see <a href="http://serverfault.com/questions/376455">why chown</a>
     */
    private void mount() throws IOException {
        this.exec("yum", "--assumeyes", "install", "nfs-utils");
        this.exec("mkdir", "-p", this.directory.getPath());
        this.exec(
            "mount",
            "inf.netbout.com:/home/ubuntu/inf",
            this.directory.getPath()
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
            final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
            final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            exec.setErrStream(stderr);
            exec.setOutputStream(stdout);
            exec.setInputStream(null);
            final int code = this.code(exec);
            if (code != 0) {
                throw new IOException(
                    String.format(
                        "Failed to execute \"%s\" (code=%d): %s",
                        command,
                        code,
                        new String(stderr.toByteArray(), CharEncoding.UTF_8)
                    )
                );
            }
            final String output = new String(
                stdout.toByteArray(),
                CharEncoding.UTF_8
            );
            exec.disconnect();
            session.disconnect();
            Logger.info(
                this,
                "#exec(..): \"%s\"\n  %s",
                command,
                output.replace("\n", "\n  ")
            );
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
                throw new IOException(ex);
            }
            // @checkstyle MagicNumber (1 line)
            if (retry > 10) {
                break;
            }
            Logger.info(this, "#pause(..): waiting for SSH (retry=%d)", retry);
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
                        return level == com.jcraft.jsch.Logger.WARN
                            || level == com.jcraft.jsch.Logger.FATAL
                            || level == com.jcraft.jsch.Logger.ERROR;
                    }
                    @Override
                    public void log(final int level, final String msg) {
                        Logger.info(NfsFolder.this.getClass(), "%s", msg);
                    }
                }
            );
            jsch.addIdentity(this.key().getPath());
            return jsch.getSession("ec2-user", "localhost");
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
        command.append("sudo -S");
        for (Object arg : args) {
            command.append(" '")
                .append(arg.toString().replace("'", "\\'"))
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
        FileUtils.forceDeleteOnExit(file);
        return file;
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
