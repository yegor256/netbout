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

import com.netbout.ih.StageFarm;
import com.netbout.inf.ebs.EbsVolume;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import com.netbout.spi.Urn;
import com.ymock.util.Logger;
import java.io.File;
import java.util.Iterator;
import org.apache.commons.io.IOUtils;

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
     * Store of predicates.
     */
    private final transient Store store;

    /**
     * Public ctor.
     */
    public DefaultInfinity() {
        this(new EbsVolume());
    }

    /**
     * Protect ctor, for tests.
     * @param fldr The folder
     */
    protected DefaultInfinity(final Folder fldr) {
        this.folder = fldr;
        this.store = new PredicateStore(this.folder);
        this.mux = new Mux(this.store);
        StageFarm.register(this);
        Logger.info(
            this,
            "#DefaultInfinity(%[type]s): instantiated (max=%d)",
            this.folder,
            this.maximum()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String statistics() {
        final StringBuilder text = new StringBuilder();
        text.append(String.format("maximum(): %s\n", this.maximum()))
            .append("Mux stats:\n")
            .append(this.mux.statistics())
            .append("\n\nStore stats:\n")
            .append(this.store.statistics())
            .append("\n\njava.lang.Runtime:\n")
            .append(
                String.format(
                    "  availableProcessors(): %d\n",
                    Runtime.getRuntime().availableProcessors()
                )
            )
            .append(
                String.format(
                    "  freeMemory(): %d\n",
                    Runtime.getRuntime().freeMemory()
                )
            )
            .append(
                String.format(
                    "  maxMemory(): %d\n",
                    Runtime.getRuntime().maxMemory()
                )
            )
            .append(
                String.format(
                    "  totalMemory(): %d\n",
                    Runtime.getRuntime().totalMemory()
                )
            );
        return text.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws java.io.IOException {
        Logger.info(this, "#close(): will stop Mux in a second");
        IOUtils.closeQuietly(this.mux);
        this.store.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long eta(final Urn who) {
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
    public Iterable<Long> messages(final String query) {
        return new LazyMessages(new PredicateBuilder(this.store).parse(query));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long maximum() {
        return this.store.maximum();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void see(final Notice notice) {
        this.mux.add(notice);
    }

}
