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
import com.netbout.inf.Cursor;
import com.netbout.inf.Msg;
import com.netbout.inf.Ray;
import com.netbout.inf.TermBuilder;
import java.io.File;
import java.io.IOException;

/**
 * In-memory implementation of {@link Ray}.
 *
 * <p>The class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class MemRay implements Ray {

    /**
     * Index map.
     */
    private final transient IndexMap imap = new DefaultIndexMap();

    /**
     * Public ctor.
     * @param dir The directory to work with
     * @throws IOException If some I/O problem
     */
    public MemRay(final File dir) throws IOException {
        // load them from file
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        Logger.info(this, "#close(): closed");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor cursor() {
        return new MemCursor(Long.MAX_VALUE, this.imap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Msg msg(final long number) {
        this.imap.touch(number);
        return this.cursor().shift(this.builder().picker(number)).msg();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TermBuilder builder() {
        return new MemTermBuilder(this.imap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long maximum() {
        return this.imap.maximum();
    }

}
