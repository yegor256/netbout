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
import com.jcabi.log.VerboseThreads;
import com.netbout.inf.Ray;
import com.netbout.inf.atoms.VariableAtom;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;

/**
 * Default implemenation of {@link IndexMap}.
 *
 * <p>This class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 * @checkstyle ClassFanOutComplexity (500 lines)
 */
@SuppressWarnings({
    "PMD.AvoidInstantiatingObjectsInLoops", "PMD.TooManyMethods"
})
final class DefaultIndexMap implements IndexMap {

    /**
     * Ray we're using.
     */
    private final transient Ray ray;

    /**
     * The map.
     */
    private final transient ConcurrentMap<String, FlushableIndex> map =
        new ConcurrentHashMap<String, FlushableIndex>();

    /**
     * All message numbers.
     */
    private final transient SortedSet<Long> all;

    /**
     * The files to work with.
     */
    private final transient Files files;

    /**
     * Public ctor.
     * @param iray The ray we're working with
     * @param dir Directory where files are kept
     * @throws IOException If some IO error
     */
    public DefaultIndexMap(final Ray iray, final File dir) throws IOException {
        final long start = System.currentTimeMillis();
        this.ray = iray;
        this.files = new Files(dir);
        final Snapshot snapshot = this.files.reader();
        final InputStream stream = new FileInputStream(snapshot.map());
        try {
            this.all = DefaultIndexMap.restore(stream);
        } finally {
            IOUtils.closeQuietly(stream);
        }
        for (String name : snapshot.attrs()) {
            FlushableIndex idx;
            if (name.equals(VariableAtom.NUMBER.attribute())) {
                idx = new ShallowIndex(this.ray);
            } else {
                idx = new DefaultIndex(this.ray, snapshot.attr(name));
            }
            this.map.put(name, idx);
        }
        Logger.info(
            this,
            // @checkstyle LineLength (1 line)
            "#DefaultIndexMap(): restored %d msgs (%[list]s) from %s at %s in %[ms]s",
            this.all.size(),
            this.map.keySet(),
            snapshot,
            dir,
            System.currentTimeMillis() - start
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Index index(final String attr) {
        if (attr == null || attr.isEmpty()) {
            throw new IllegalArgumentException("attribute name is empty");
        }
        if (!this.map.containsKey(attr)) {
            FlushableIndex idx;
            if (attr.equals(VariableAtom.NUMBER.attribute())) {
                idx = new ShallowIndex(this.ray);
            } else {
                idx = new DefaultIndex(this.ray);
            }
            this.map.putIfAbsent(attr, idx);
        }
        return this.map.get(attr);
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
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        text.append(String.format("%d msgs\n", this.all.size()));
        final String[] attrs = new String[] {
            com.netbout.inf.atoms.VariableAtom.NUMBER.attribute(),
            com.netbout.inf.atoms.VariableAtom.BOUT_NUMBER.attribute(),
            com.netbout.inf.atoms.VariableAtom.AUTHOR_NAME.attribute(),
            "talks-with",
            "bundled-marker",
        };
        return text.toString();
    }

    /**
     * Save map to disc.
     * @throws IOException If some problem
     */
    public void flush() throws IOException {
        final long start = System.currentTimeMillis();
        final ExecutorService service = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            new VerboseThreads("imap-flush")
        );
        final Snapshot snapshot = this.files.writer();
        final Collection<Callable<Void>> tasks =
            new ArrayList<Callable<Void>>(this.map.size() + 1);
        tasks.add(
            new Callable<Void>() {
                public Void call() throws IOException {
                    final OutputStream stream =
                        new FileOutputStream(snapshot.map());
                    try {
                        final PrintWriter writer = new PrintWriter(
                            new OutputStreamWriter(stream, CharEncoding.UTF_8)
                        );
                        for (Long number : DefaultIndexMap.this.all) {
                            writer.println(number.toString());
                        }
                        writer.flush();
                    } finally {
                        IOUtils.closeQuietly(stream);
                    }
                    return null;
                }
            }
        );
        for (final String attr : this.map.keySet()) {
            tasks.add(
                new Callable<Void>() {
                    public Void call() throws IOException {
                        DefaultIndexMap.this.map.get(attr)
                            .flush(snapshot.attr(attr));
                        return null;
                    }
                }
            );
        }
        try {
            for (Future<Void> future : service.invokeAll(tasks)) {
                future.get();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException(ex);
        } catch (java.util.concurrent.ExecutionException ex) {
            throw new IOException(ex);
        }
        service.shutdown();
        this.files.publish(snapshot);
        Logger.info(
            this,
            "#save(): saved %d messages to %s in %[ms]s",
            this.all.size(),
            snapshot,
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
        return numbers;
    }

}
