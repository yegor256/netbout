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
import com.jcabi.log.VerboseThreads;
import com.netbout.inf.Attribute;
import com.netbout.inf.atoms.VariableAtom;
import com.netbout.inf.ray.Index;
import com.netbout.inf.ray.IndexMap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
public final class DefaultIndexMap implements IndexMap {

    /**
     * Associative collection of indexes.
     */
    private final transient ConcurrentMap<Attribute, FlushableIndex> map =
        new ConcurrentHashMap<Attribute, FlushableIndex>();

    /**
     * The directory to work with.
     */
    private final transient Directory directory;

    /**
     * Public ctor.
     * @param dir Directory where files are kept
     * @throws IOException If some IO error
     */
    public DefaultIndexMap(final File dir) throws IOException {
        this.directory = new DefaultDirectory(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        for (Attribute attr : this.map.keySet()) {
            text.append(attr.toString())
                .append(": ")
                .append(this.map.get(attr).toString())
                .append("\n");
        }
        long sizeof = 0L;
        for (FlushableIndex idx : this.map.values()) {
            sizeof += idx.sizeof();
        }
        return text.append(String.format("total size: %,d\n", sizeof))
            .append(this.directory.toString())
            .toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.directory.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.ConfusingTernary")
    public Index index(final Attribute attr) throws IOException {
        if (!this.map.containsKey(attr)) {
            FlushableIndex idx;
            if (attr.getClass().getAnnotation(Attribute.Reversive.class)
                != null) {
                idx = new ReversiveIndex(attr, this.directory);
            } else if (attr.getClass().getAnnotation(Attribute.Mirroring.class)
                != null) {
                idx = new NumbersIndex(attr, this.directory);
            } else {
                idx = new BaseIndex(attr, this.directory);
            }
            this.map.putIfAbsent(attr, idx);
        }
        return this.map.get(attr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void touch(final long msg) {
        try {
            this.index(VariableAtom.NUMBER.attribute()).add(msg, "");
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        final long start = System.currentTimeMillis();
        final ExecutorService service = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            new VerboseThreads("imap-flush")
        );
        final Collection<Callable<Void>> tasks =
            new ArrayList<Callable<Void>>(this.map.size());
        for (final FlushableIndex index : this.map.values()) {
            tasks.add(
                new Callable<Void>() {
                    public Void call() throws IOException {
                        index.flush();
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
        this.directory.baseline();
        Logger.info(
            this,
            "#flush(): saved %d indexes %[list]s to %s in %[ms]s",
            this.map.size(),
            this.map.keySet(),
            this.directory,
            System.currentTimeMillis() - start
        );
    }

}
