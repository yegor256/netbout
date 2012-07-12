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

import com.netbout.inf.Attribute;
import com.netbout.inf.Lattice;
import com.netbout.inf.ray.imap.dir.SimpleNumbers;
import java.io.IOException;

/**
 * Index of message numbers.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
final class NumbersIndex implements FlushableIndex {

    /**
     * Attribute we use.
     */
    private final transient Attribute attribute;

    /**
     * Directory to work with.
     */
    private final transient Directory directory;

    /**
     * All numbers.
     */
    private final transient Numbers numbers = new SimpleNumbers();

    /**
     * Public ctor.
     * @param attr The attribute
     * @param dir The directory to work with
     * @throws IOException If some IO error
     */
    public NumbersIndex(final Attribute attr, final Directory dir)
        throws IOException {
        this.attribute = attr;
        this.directory = dir;
        this.directory.load(
            this.attribute,
            this.attribute.toString(),
            this.numbers
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long sizeof() {
        return this.numbers.sizeof();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "%,d bytes in numbers",
            this.numbers.sizeof()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replace(final long msg, final String value) {
        this.numbers.add(msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final long msg, final String value) {
        this.numbers.add(msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final long msg, final String value) {
        throw new UnsupportedOperationException("#delete()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clean(final long msg) {
        throw new UnsupportedOperationException("#clean()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String attr(final long msg) {
        if (this.numbers.next(msg + 1) != msg) {
            throw new IllegalArgumentException(
                String.format("no message with number #%d", msg)
            );
        }
        return Long.toString(msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Lattice lattice(final String value) {
        return this.numbers.lattice();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long next(final String value, final long msg) {
        return this.numbers.next(msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        this.directory.save(
            this.attribute,
            this.attribute.toString(),
            this.numbers
        );
    }

}
