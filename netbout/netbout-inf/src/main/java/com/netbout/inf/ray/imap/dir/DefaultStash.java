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
import com.netbout.inf.Notice;
import com.netbout.inf.Stash;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Default implementation of {@link Stash}.
 *
 * <p>Class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class DefaultStash implements Stash {

    /**
     * Lock on the directory.
     */
    private final transient Lock lock;

    /**
     * Public ctor.
     * @param path The directory where to save files
     * @throws IOException If some I/O problem inside
     */
    public DefaultStash(final File path) throws IOException {
        this.lock = new Lock(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.lock.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(Notice notice) throws IOException {
        final Notice.SerializableNotice ser =
            new Notice.SerializableNotice(notice);
        final byte[] bytes = ser.serialize();
        final File file = this.file(ser);
        FileUtils.writeByteArrayToFile(file, bytes);
        Logger.debug(
            this,
            "#add(%[type]s): saved %d bytes into %s",
            notice,
            bytes.length,
            FilenameUtils.getName(file.getPath())
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(Notice notice) throws IOException {
        final File file = this.file(new Notice.SerializableNotice(notice));
        file.delete();
        Logger.debug(
            this,
            "#remove(%[type]s): deleted %s",
            notice,
            FilenameUtils.getName(file.getPath())
        );
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returned iterator is thread-safe.
     */
    @Override
    public Iterator<Notice> iterator() throws IOException {
        return new Iterator<Notice>() {
            private final transient AtomicReference<File> file =
                new AtomicReference<File>();
            @Override
            public boolean hasNext() {
                if (this.file.get() == null) {
                    File[] files;
                    try {
                        files = DefaultStash.this.lock.dir().listFiles();
                    } catch (java.io.IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                    for (File candidate : files) {
                        if (FilenameUtils.getName(candidate.getPath())
                            .startsWith("ntc-")) {
                            this.file.set(candidate);
                            break;
                        }
                        Logger.debug(
                            this,
                            "#hasNext(): %s ignored",
                            FilenameUtils.getName(candidate.getPath())
                        );
                    }
                    Logger.debug(
                        this,
                        "#hasNext(): found %d files, %s is next",
                        files.length,
                        this.file.get()
                    );
                }
                return this.file.get() != null;
            }
            @Override
            public Notice next() {
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                }
                Notice notice;
                byte[] bytes;
                final File ntc = this.file.getAndSet(null);
                try {
                    bytes = FileUtils.readFileToByteArray(ntc);
                    notice = Notice.SerializableNotice.deserialize(bytes);
                } catch (java.io.IOException ex) {
                    throw new IllegalStateException(ex);
                }
                Logger.debug(
                    this,
                    "#next(): loaded %[type]s from %s (%d bytes)",
                    notice,
                    FilenameUtils.getName(ntc.getPath()),
                    bytes.length
                );
                return notice;
            }
            @Override
            public void remove() {
                if (this.file.get() == null) {
                    throw new NoSuchElementException();
                }
                final File ntc = this.file.getAndSet(null);
                ntc.delete();
                Logger.debug(
                    this,
                    "#remove(): deleted %s",
                    FilenameUtils.getName(ntc.getPath())
                );
            }
        };
    }

    /**
     * Create file name of the notice.
     * @param notice The notice
     * @return The file name
     * @throws IOException If some error
     */
    private File file(final Notice.SerializableNotice notice)
        throws IOException {
        return new File(
            this.lock.dir(),
            String.format(
                "ntc-%s.ser",
                StringUtils.stripEnd(
                    Base64.encodeBase64String(notice.toString().getBytes()),
                    "="
                )
            )
        );
    }

}
