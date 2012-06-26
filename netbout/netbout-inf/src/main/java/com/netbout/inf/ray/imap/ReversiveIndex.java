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

import com.jcabi.log.Logger;
import com.netbout.inf.Attribute;
import com.netbout.inf.Lattice;
import com.netbout.inf.ray.Index;
import com.netbout.inf.ray.IndexMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;

/**
 * Reversive implemenation of {@link Index}.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
class ReversiveIndex implements FlushableIndex {

    /**
     * The attribute.
     */
    private final transient Attribute attribute;

    /**
     * Straight index.
     */
    private final transient FlushableIndex straight;

    /**
     * Reverse.
     */
    private final transient Reverse reverse;

    /**
     * Directory to work with.
     */
    private final transient Directory directory;

    /**
     * Public ctor.
     * @param attr The attribute
     * @param dir The directory
     * @throws IOException If some IO error
     */
    public ReversiveIndex(final Attribute attr,
        final Directory dir) throws IOException {
        this.attribute = attr;
        this.directory = dir;
        this.straight = new BaseIndex(this.attribute, this.directory);
        this.reverse = new SimpleReverse();
        this.directory.load(this.attribute, this.reverse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replace(final long msg, final String value) {
        this.straight.replace(msg, value);
        this.reverse.put(msg, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final long msg, final String value) {
        this.straight.add(msg, value);
        this.reverse.put(msg, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final long msg, final String value) {
        this.straight.delete(msg, value);
        this.reverse.remove(msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clean(final long msg) {
        this.straight.clean(msg);
        this.reverse.remove(msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Lattice lattice(final String value) {
        return this.straight.lattice(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long next(final String value, final long msg) {
        return this.straight.next(value, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String attr(final long msg) {
        return this.reverse.get(msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        this.straight.flush();
        this.directory.save(this.attribute, reverse);
    }

}
