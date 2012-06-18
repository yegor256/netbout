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
import com.netbout.inf.Lattice;
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
 * Default implemenation of {@link Index}.
 *
 * <p>This class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings({
    "PMD.TooManyMethods", "PMD.AvoidInstantiatingObjectsInLoops"
})
final class DefaultIndex implements FlushableIndex {

    /**
     * Main map.
     */
    private final transient ConcurrentMap<String, SortedSet<Long>> map;

    /**
     * Lattices.
     */
    private final transient ConcurrentMap<String, DefaultLattice> lattices;

    /**
     * Reverse map.
     */
    private final transient ConcurrentMap<Long, String> rmap;

    /**
     * Public ctor.
     */
    public DefaultIndex() {
        this.map = new ConcurrentHashMap<String, SortedSet<Long>>();
        this.rmap = new ConcurrentHashMap<Long, String>();
        this.lattices = new ConcurrentHashMap<String, DefaultLattice>();
    }

    /**
     * Public ctor.
     * @param file File to read from
     * @throws IOException If some IO error
     */
    public DefaultIndex(final File file) throws IOException {
        final InputStream stream = new FileInputStream(file);
        try {
            final long start = System.currentTimeMillis();
            this.map = DefaultIndex.restore(stream);
            this.rmap = DefaultIndex.reverse(this.map);
            this.lattices = new ConcurrentHashMap<String, DefaultLattice>();
            for (ConcurrentMap.Entry<String, SortedSet<Long>> entry
                : this.map.entrySet()) {
                this.lattices.put(
                    entry.getKey(),
                    new DefaultLattice(entry.getValue())
                );
            }
            Logger.debug(
                DefaultIndex.class,
                "#DefaultIndex(%s): restored %d values from %d bytes in %[ms]s",
                file.getName(),
                this.map.size(),
                file.length(),
                System.currentTimeMillis() - start
            );
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replace(final long msg, final String value) {
        this.validate(msg);
        this.clean(msg);
        this.add(msg, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final long msg, final String value) {
        this.validate(msg);
        this.numbers(value).add(msg);
        this.rmap.put(msg, value);
        this.lattice(value).set(
            msg,
            true,
            DefaultLattice.emptyBit(this.map.get(value), msg)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final long msg, final String value) {
        this.validate(msg);
        this.numbers(value).remove(msg);
        this.rmap.remove(msg);
        this.lattice(value).set(
            msg,
            false,
            DefaultLattice.emptyBit(this.map.get(value), msg)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clean(final long msg) {
        for (String value : this.map.keySet()) {
            this.delete(msg, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String first(final long msg) {
        String val = null;
        int count = 0;
        // @checkstyle MagicNumber (1 line)
        while (++count < 15) {
            val = this.rmap.get(msg);
            if (val != null) {
                break;
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            }
        }
        if (val == null) {
            throw new IllegalArgumentException(
                String.format("attribute not found for msg #%d", msg)
            );
        }
        return val;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Long> msgs(final String value) {
        return Collections.unmodifiableSortedSet(this.numbers(value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> values() {
        return this.map.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Lattice lattice(final String value) {
        this.lattices.putIfAbsent(value, new DefaultLattice());
        return this.lattices.get(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush(final File file) throws IOException {
        final long start = System.currentTimeMillis();
        final OutputStream stream = new FileOutputStream(file);
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
        Logger.debug(
            this,
            "#flush(): saved %d values to %s (%d bytes) in %[ms]s",
            this.map.size(),
            file.getName(),
            file.length(),
            System.currentTimeMillis() - start
        );
    }

    /**
     * Restore map from stream.
     * @param stream The stream to read from
     * @return The data restored
     * @throws IOException If some IO error
     */
    private static ConcurrentMap<String, SortedSet<Long>> restore(
        final InputStream stream) throws IOException {
        final ConcurrentMap<String, SortedSet<Long>> data =
            new ConcurrentHashMap<String, SortedSet<Long>>();
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
                    new ConcurrentSkipListSet<Long>(Collections.reverseOrder())
                );
            }
        }
        return data;
    }

    /**
     * Reverse the map.
     * @param origin Original map
     * @return The reversed one
     */
    private static ConcurrentMap<Long, String> reverse(
        final ConcurrentMap<String, SortedSet<Long>> origin) {
        final ConcurrentMap<Long, String> data =
            new ConcurrentHashMap<Long, String>();
        for (ConcurrentMap.Entry<String, SortedSet<Long>> entry
            : origin.entrySet()) {
            for (Long number : entry.getValue()) {
                data.putIfAbsent(number, entry.getKey());
            }
        }
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

    /**
     * Numbers for the given value.
     * @param text The text value
     * @return Numbers (link to existing structure in the MAP)
     */
    private SortedSet<Long> numbers(final String text) {
        this.map.putIfAbsent(
            text,
            new ConcurrentSkipListSet<Long>(Collections.reverseOrder())
        );
        return this.map.get(text);
    }

}
