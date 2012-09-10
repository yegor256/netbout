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

import com.netbout.inf.Attribute;
import com.netbout.inf.Notice;
import com.netbout.inf.Stash;
import com.netbout.inf.ray.imap.Directory;
import com.netbout.inf.ray.imap.Numbers;
import com.netbout.inf.ray.imap.Reverse;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

/**
 * Concurrent implementation of {@link Directory}.
 *
 * <p>Class is mutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id: ConcurrentDirectory.java 3260 2012-08-20 19:15:11Z yegor@tpc2.com $
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class ConcurrentDirectory implements Directory {

    /**
     * Total number of semaphores (the number is related to the number of
     * processors and should be rather big to allow as many as necessary
     * threads to do parallel manipulations).
     */
    private static final int SEMAPHORES =
        Runtime.getRuntime().availableProcessors() * 25;

    /**
     * Semaphore, that holds locks for every actively working load/save task.
     */
    private final transient Semaphore semaphore = new Semaphore(
        ConcurrentDirectory.SEMAPHORES, true
    );

    /**
     * Default directory.
     */
    private final transient Directory dir;

    /**
     * Public ctor.
     * @param file The directory
     * @throws IOException If some I/O problem inside
     */
    public ConcurrentDirectory(final File file) throws IOException {
        this.dir = new DefaultDirectory(file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        this.acquire(1);
        try {
            return this.dir.toString();
        } finally {
            this.semaphore.release();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(final Attribute attr, final String value,
        final Numbers nums) throws IOException {
        this.acquire(1);
        try {
            this.dir.save(attr, value, nums);
        } finally {
            this.semaphore.release();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(final Attribute attr, final String value,
        final Numbers nums) throws IOException {
        this.acquire(1);
        try {
            this.dir.load(attr, value, nums);
        } finally {
            this.semaphore.release();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(final Attribute attr,
        final Reverse reverse) throws IOException {
        this.acquire(1);
        try {
            this.dir.save(attr, reverse);
        } finally {
            this.semaphore.release();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(final Attribute attr,
        final Reverse reverse) throws IOException {
        this.acquire(1);
        try {
            this.dir.load(attr, reverse);
        } finally {
            this.semaphore.release();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void baseline() throws IOException {
        this.acquire(ConcurrentDirectory.SEMAPHORES);
        try {
            this.dir.baseline();
        } finally {
            this.semaphore.release(ConcurrentDirectory.SEMAPHORES);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.acquire(ConcurrentDirectory.SEMAPHORES);
        try {
            this.dir.close();
        } finally {
            this.semaphore.release(ConcurrentDirectory.SEMAPHORES);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Stash stash() throws IOException {
        // @checkstyle AnonInnerLength (50 lines)
        return new Stash() {
            @Override
            public void add(final Notice notice) throws IOException {
                ConcurrentDirectory.this.acquire(1);
                try {
                    ConcurrentDirectory.this.dir.stash().add(notice);
                } finally {
                    ConcurrentDirectory.this.semaphore.release();
                }
            }
            @Override
            public void remove(final Notice notice) throws IOException {
                ConcurrentDirectory.this.acquire(1);
                try {
                    ConcurrentDirectory.this.dir.stash().remove(notice);
                } finally {
                    ConcurrentDirectory.this.semaphore.release();
                }
            }
            @Override
            public void copyTo(final Stash stash) throws IOException {
                ConcurrentDirectory.this.acquire(1);
                try {
                    ConcurrentDirectory.this.dir.stash().copyTo(stash);
                } finally {
                    ConcurrentDirectory.this.semaphore.release();
                }
            }
            @Override
            public Iterator<Notice> iterator() {
                ConcurrentDirectory.this.acquire(1);
                try {
                    return ConcurrentDirectory.this.dir.stash().iterator();
                } catch (java.io.IOException ex) {
                    throw new IllegalStateException(ex);
                } finally {
                    ConcurrentDirectory.this.semaphore.release();
                }
            }
            @Override
            public void close() throws IOException {
                ConcurrentDirectory.this.acquire(1);
                try {
                    ConcurrentDirectory.this.dir.stash().close();
                } finally {
                    ConcurrentDirectory.this.semaphore.release();
                }
            }
        };
    }

    /**
     * Acquire a few semaphores.
     * @param num How many to acquire
     */
    private void acquire(final int num) {
        try {
            this.semaphore.acquire(num);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }

}
