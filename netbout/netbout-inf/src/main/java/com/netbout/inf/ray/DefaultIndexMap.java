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
import java.io.BufferedReader;
import java.io.Closeable;
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
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;

/**
 * Index map.
 *
 * <p>This class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
final class DefaultIndexMap implements IndexMap, Closeable {

    /**
     * The map.
     */
    private final transient ConcurrentMap<String, DefaultIndex> map =
        new ConcurrentHashMap<String, DefaultIndex>();

    /**
     * All message numbers.
     */
    private final transient SortedSet<Long> all;

    /**
     * The file to work with.
     */
    private final transient File file;

    /**
     * Public ctor.
     * @param dir Directory where files are kept
     * @throws IOException If some IO error
     */
    public DefaultIndexMap(final File dir) throws IOException {
        this.file = new File(dir, "index-map.txt");
        FileUtils.touch(this.file);
        final InputStream stream = new FileInputStream(this.file);
        try {
            this.all = DefaultIndexMap.restore(stream);
        } finally {
            IOUtils.closeQuietly(stream);
        }
        final Pattern pattern = Pattern.compile("attr-(.*?)\\.txt");
        for (String name : dir.list()) {
            final Matcher matcher = pattern.matcher(name);
            if (matcher.matches()) {
                this.index(matcher.group(1));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Index index(final String attr) {
        if (attr == null || attr.isEmpty()) {
            throw new IllegalArgumentException("attribute name is empty");
        }
        if (this.map.get(attr) == null) {
            try {
                this.map.putIfAbsent(
                    attr,
                    new DefaultIndex(
                        new File(
                            this.file.getParentFile(),
                            String.format("attr-%s.txt", attr)
                        )
                    )
                );
            } catch (java.io.IOException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
        return this.map.get(attr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.flush();
        for (DefaultIndex index : this.map.values()) {
            index.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void touch(final long number) {
        if (number == 0L) {
            throw new IllegalArgumentException("msg number can't be ZERO");
        }
        if (number == Long.MAX_VALUE) {
            throw new IllegalArgumentException("msg number can't be MAX_VALUE");
        }
        this.all.add(number);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Long> msgs() {
        return Collections.unmodifiableSortedSet(this.all);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long maximum() {
        long max;
        if (this.all.isEmpty()) {
            max = 0L;
        } else {
            max = this.all.first();
        }
        return max;
    }

    /**
     * Save map to disc.
     * @throws IOException If some problem
     */
    public void flush() throws IOException {
        final long start = System.currentTimeMillis();
        final OutputStream stream = new FileOutputStream(this.file);
        try {
            final PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(stream, CharEncoding.UTF_8)
            );
            for (Long number : this.all) {
                writer.println(number.toString());
            }
            writer.flush();
        } finally {
            IOUtils.closeQuietly(stream);
        }
        for (DefaultIndex index : this.map.values()) {
            index.flush();
        }
        Logger.info(
            this,
            "#save(): saved %d msg numbers to %s (%d bytes) in %[ms]s",
            this.all.size(),
            this.file,
            this.file.length(),
            System.currentTimeMillis() - start
        );
    }

    /**
     * Restore map.
     * @param stream Where to read from
     * @return The data restored
     * @throws IOException If some IO error
     */
    private static SortedSet<Long> restore(final InputStream stream)
        throws IOException {
        final SortedSet<Long> numbers =
            new ConcurrentSkipListSet<Long>(Collections.reverseOrder());
        final long start = System.currentTimeMillis();
        final BufferedReader reader = new BufferedReader(
            new InputStreamReader(stream, CharEncoding.UTF_8)
        );
        while (true) {
            final String line = reader.readLine();
            if (line == null || line.isEmpty()) {
                break;
            }
            numbers.add(Long.valueOf(line));
        }
        Logger.info(
            DefaultIndexMap.class,
            "#restore(): restored %d msg numbers in %[ms]s",
            numbers.size(),
            System.currentTimeMillis() - start
        );
        return numbers;
    }

}
