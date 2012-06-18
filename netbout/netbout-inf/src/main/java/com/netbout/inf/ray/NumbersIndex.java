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
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;

/**
 * One-to-one {@link Index}.
 *
 * <p>This class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
final class NumbersIndex implements FlushableIndex {

    /**
     * Main map.
     */
    private final transient SortedSet<Long> numbers =
        new ConcurrentSkipListSet<Long>(Collections.reverseOrder());

    /**
     * Public ctor.
     * @param file File to read from
     * @throws IOException If some IO error
     */
    public NumbersIndex(final File file) throws IOException {
        final InputStream stream = new FileInputStream(file);
        try {
            final long start = System.currentTimeMillis();
            final BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, CharEncoding.UTF_8)
            );
            while (true) {
                final String line = reader.readLine();
                if (line == null || line.isEmpty()) {
                    break;
                }
                if (line.charAt(0) != ' ') {
                    this.numbers.add(Long.valueOf(line));
                }
            }
            Logger.debug(
                DefaultIndex.class,
                "#NumbersIndex(%s): restored %d nums from %d bytes in %[ms]s",
                file.getName(),
                this.numbers.size(),
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
        throw new UnsupportedOperationException("#replace()");
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
    public String first(final long msg) {
        return Long.toString(msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Long> msgs(final String value) {
        return new TreeSet<Long>(Arrays.asList(Long.valueOf(value)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> values() {
        throw new UnsupportedOperationException("#values()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Lattice lattice(final String value) {
        throw new UnsupportedOperationException("#lattice()");
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
            for (Long num : this.numbers) {
                writer.println(num);
                writer.print("\n ");
                writer.println(num.toString());
                writer.print('\n');
            }
            writer.flush();
        } finally {
            IOUtils.closeQuietly(stream);
        }
        Logger.debug(
            this,
            "#flush(): saved %d numbers to %s (%d bytes) in %[ms]s",
            this.numbers.size(),
            file.getName(),
            file.length(),
            System.currentTimeMillis() - start
        );
    }

}
