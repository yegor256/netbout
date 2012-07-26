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
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * Lock of the directory.
 *
 * <p>Class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class Lock implements Closeable {

    /**
     * Name of the lock file.
     */
    private static final String NAME = "lock.txt";

    /**
     * The directory itself.
     */
    private final transient File directory;

    /**
     * Lock.
     */
    private final transient FileOutputStream stream;

    /**
     * Channel.
     */
    private final transient FileChannel channel;

    /**
     * The lock itself.
     */
    private final transient FileLock filelock;

    /**
     * Public ctor.
     * @param dir The directory to lock
     * @throws IOException If some I/O problem inside or this directory is
     *  already locked by another thread/class
     */
    public Lock(final File dir) throws IOException {
        this.directory = dir;
        final File file = new File(this.directory, Lock.NAME);
        file.getParentFile().mkdirs();
        if (file.exists()) {
            Logger.warn(
                this,
                "#Lock('%s'): trying to clean a dirty lock '$s'",
                FilenameUtils.getName(this.directory.getPath()),
                FileUtils.readFileToString(file)
            );
        }
        this.stream = new FileOutputStream(file);
        new PrintStream(this.stream).println("locked");
        this.channel = this.stream.getChannel();
        try {
            this.filelock = this.channel.lock();
        } catch (java.nio.channels.OverlappingFileLockException ex) {
            throw new IOException(ex);
        }
        Logger.debug(
            this,
            "#Lock('/%s/%s'): locked by %s",
            FilenameUtils.getName(this.directory.getParent()),
            FilenameUtils.getName(this.directory.getPath()),
            super.toString()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.directory.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object lck) {
        return this == lck || (lck instanceof Lock
            && Lock.class.cast(lck).directory.equals(this.directory));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "lock:/%s",
            FilenameUtils.getName(this.directory.getPath())
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.filelock.release();
        this.channel.close();
        this.stream.close();
        new File(this.directory, Lock.NAME).delete();
        Logger.debug(
            this,
            "#close(): '/%s' unlocked by %s",
            FilenameUtils.getName(this.directory.getPath()),
            super.toString()
        );
    }

    /**
     * Get directory or throw exception if it's not locked.
     * @return The directory
     * @throws IOException If some I/O problem of the directory is not locked
     */
    public File dir() throws IOException {
        if (!this.filelock.isValid()) {
            throw new IOException("closed lock");
        }
        return this.directory;
    }

    /**
     * Expire it, mark the directory as expired.
     * @throws IOException If some I/O problem inside
     */
    public void expire() throws IOException {
        this.directory.renameTo(
            new File(
                this.directory.getParentFile(),
                String.format(
                    "%s-expired",
                    FilenameUtils.getName(this.directory.getPath())
                )
            )
        );
        Logger.debug(
            this,
            "#expire(): '/%s' expired",
            FilenameUtils.getName(this.directory.getPath())
        );
    }

    /**
     * Delete all files in the directory.
     * @throws IOException If some I/O problem inside
     */
    public void clear() throws IOException {
        int deleted = 0;
        for (File file : this.directory.listFiles()) {
            if (Lock.NAME.equals(FilenameUtils.getName(file.getPath()))) {
                continue;
            }
            FileUtils.forceDelete(file);
            ++deleted;
        }
        Logger.debug(
            this,
            "#clear(): %d file(s) removed in '/%s'",
            deleted,
            FilenameUtils.getName(this.directory.getPath())
        );
    }

}
