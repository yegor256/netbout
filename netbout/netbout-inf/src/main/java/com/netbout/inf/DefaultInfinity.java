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
package com.netbout.inf;

import com.jcabi.log.Logger;
import com.netbout.ih.StageFarm;
import com.netbout.inf.functors.DefaultStore;
import com.netbout.inf.ray.MemRay;
import com.netbout.spi.Urn;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.apache.commons.io.FileUtils;

/**
 * Default implementation of Infitity.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class DefaultInfinity implements Infinity {

    /**
     * Multiplexer of tasks.
     */
    private final transient Mux mux;

    /**
     * The folder to work with.
     */
    private final transient Folder folder;

    /**
     * Store of functors.
     */
    private final transient Store store = new DefaultStore();

    /**
     * Ray of messages.
     */
    private final transient Ray ray;

    /**
     * Public ctor.
     * @throws IOException If some IO problem
     */
    public DefaultInfinity() throws IOException {
        this(new NfsFolder(new File("/mnt/inf")));
    }

    /**
     * Protect ctor, for tests.
     * @param fldr The folder
     * @throws IOException If some IO problem
     */
    protected DefaultInfinity(final Folder fldr) throws IOException {
        this.folder = fldr;
        this.ray = new MemRay(new File(this.folder.path(), "ray"));
        this.mux = new Mux(this.ray, this.store);
        StageFarm.register(this);
        Logger.info(
            this,
            "#DefaultInfinity(%s): instantiated (max=%d)",
            this.folder.path(),
            this.maximum()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        text.append(String.format("maximum(): %s\n", this.maximum()))
            .append("Mux stats:\n")
            .append(this.mux)
            .append("\n\nStore stats:\n")
            .append(this.store)
            .append("\n\nRay stats:\n")
            .append(this.ray)
            .append("\n\njava.lang.Runtime:\n")
            .append(
                String.format(
                    "  availableProcessors(): %d\n",
                    Runtime.getRuntime().availableProcessors()
                )
            )
            .append(
                String.format(
                    "  freeMemory(): %s\n",
                    FileUtils.byteCountToDisplaySize(
                        Runtime.getRuntime().freeMemory()
                    )
                )
            )
            .append(
                String.format(
                    "  maxMemory(): %s\n",
                    FileUtils.byteCountToDisplaySize(
                        Runtime.getRuntime().maxMemory()
                    )
                )
            )
            .append(
                String.format(
                    "  totalMemory(): %s\n",
                    FileUtils.byteCountToDisplaySize(
                        Runtime.getRuntime().totalMemory()
                    )
                )
            );
        return text.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.mux.close();
        this.ray.close();
        this.folder.close();
        Logger.info(this, "#close(): closed");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        this.ray.flush();
    }

    /**
     * {@inheritDoc}
     *
     * <p>We should return ONE in case the Mux is not yet ready and we don't
     * have any tasks there and no data is in INF. It means that the Infinity
     * hasn't been intialized yet.
     */
    @Override
    public long eta(final Urn... who) {
        long eta = this.mux.eta(who);
        if (eta == 0 && this.maximum() == 0) {
            eta = 1;
        }
        return eta;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Long> messages(final String query)
        throws InvalidSyntaxException {
        final Term term = new ParserAdapter(this.store)
            .parse(query)
            .term(this.ray);
        Logger.debug(this, "#messages('%[text]s'): term '%s'", query, term);
        return new LazyMessages(this.ray, term);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long maximum() {
        return this.ray.maximum();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Urn> see(final Notice notice) {
        try {
            return this.mux.add(notice);
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

}
