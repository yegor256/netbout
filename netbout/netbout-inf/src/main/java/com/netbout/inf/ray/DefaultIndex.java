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
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;

/**
 * Index.
 *
 * <p>This class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
final class DefaultIndex implements Index, Closeable {

    /**
     * The map.
     */
    private final transient ConcurrentMap<String, SortedSet<Long>> map;

    /**
     * The file to work with.
     */
    private final transient File file;

    /**
     * Public ctor.
     * @param path File where to keep it
     * @throws IOException If some IO error
     */
    public DefaultIndex(final File path) throws IOException {
        this.file = path;
        FileUtils.touch(this.file);
        final InputStream stream = new FileInputStream(this.file);
        try {
            this.map = DefaultIndex.restore(stream);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replace(final long msg, final String value) {
        this.validate(msg);
        this.clean(msg);
        this.msgs(value).add(msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final long msg, final String value) {
        this.validate(msg);
        this.msgs(value).add(msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final long msg, final String value) {
        this.validate(msg);
        final SortedSet<Long> set = this.msgs(value);
        if (set.contains(msg)) {
            set.remove(msg);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clean(final long msg) {
        this.validate(msg);
        for (SortedSet<Long> set : this.map.values()) {
            if (set.contains(msg)) {
                set.remove(msg);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> values(final long msg) {
        this.validate(msg);
        final Set<String> values = new HashSet<String>();
        for (ConcurrentMap.Entry<String, SortedSet<Long>> entry
            : this.map.entrySet()) {
            if (entry.getValue().contains(msg)) {
                values.add(entry.getKey());
            }
        }
        return values;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Long> msgs(final String value) {
        if (this.map.get(value) == null) {
            this.map.putIfAbsent(
                value,
                new ConcurrentSkipListSet(Collections.reverseOrder())
            );
        }
        return this.map.get(value);
    }

    /**
     * Flush this map to disc.
     * @throws IOException If some problem
     */
    public void flush() throws IOException {
        final long start = System.currentTimeMillis();
        final OutputStream stream = new FileOutputStream(this.file);
        try {
            final PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(stream, CharEncoding.UTF_8)
            );
            for (String value : this.map.keySet()) {
                writer.println(value);
                for (Long number : this.map.get(value)) {
                    writer.print(' ');
                    writer.println(number.toString());
                }
            }
            writer.flush();
        } finally {
            IOUtils.closeQuietly(stream);
        }
        Logger.info(
            this,
            "#save(): saved %d values to %s (%d bytes) in %[ms]s",
            this.map.size(),
            this.file,
            this.file.length(),
            System.currentTimeMillis() - start
        );
    }

    /**
     * Restore map from stream.
     * @param stream The stream to read from
     * @return The data restored
     * @throws IOException If some IO error
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private static ConcurrentMap<String, SortedSet<Long>> restore(
        final InputStream stream) throws IOException {
        final ConcurrentMap<String, SortedSet<Long>> data =
            new ConcurrentHashMap<String, SortedSet<Long>>();
        final long start = System.currentTimeMillis();
        final BufferedReader reader = new BufferedReader(
            new InputStreamReader(stream, CharEncoding.UTF_8)
        );
        String value = null;
        while (true) {
            final String line = reader.readLine();
            if (line == null || line.isEmpty()) {
                break;
            }
            if (line.charAt(0) == ' ') {
                data.get(value).add(Long.valueOf(line.substring(1)));
            } else {
                value = line;
                data.put(
                    value,
                    new ConcurrentSkipListSet(Collections.reverseOrder())
                );
            }
        }
        Logger.info(
            DefaultIndex.class,
            "#restore(): restored %d values in %[ms]s",
            data.size(),
            System.currentTimeMillis() - start
        );
        return data;
    }

    /**
     * Validate this message number and throw runtime exception if it's not
     * valid (is ZERO or MAX_VALUE).
     * @param msg The number of msg
     */
    private void validate(final long msg) {
        if (msg == 0L) {
            throw new IllegalArgumentException("msg number can't be ZERO");
        }
        if (msg == Long.MAX_VALUE) {
            throw new IllegalArgumentException("msg number can't be MAX_VALUE");
        }
    }

}
