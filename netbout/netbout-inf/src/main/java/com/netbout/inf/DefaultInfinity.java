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

import com.netbout.bus.Bus;
import com.netbout.inf.ih.StageFarm;
import com.netbout.inf.index.FsIndex;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import com.netbout.spi.Urn;
import com.ymock.util.Logger;

/**
 * Default implementation of Infitity.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class DefaultInfinity implements Infinity {

    /**
     * The bus.
     */
    private final transient Bus bus;

    /**
     * Multiplexer of tasks.
     */
    private final transient Mux mux = new Mux();

    /**
     * Store of predicates.
     */
    private final transient Store store = new PredicateStore();

    /**
     * Public ctor, with custom index.
     * @param ibus The BUS to work with
     */
    public DefaultInfinity(final Bus ibus) {
        this.bus = ibus;
        StageFarm.register(this);
        Logger.info(this, "#DefaultInfinity(%[type]s): instantiated", ibus);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String statistics() {
        final StringBuilder text = new StringBuilder();
        text.append("Mux stats:\n")
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
        this.mux.close();
        this.store.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long eta(final Urn who) {
        return this.mux.eta(who);
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
    public void see(final Message message) {
        this.mux.add(new SeeMessageTask(message, this.store));
        Logger.debug(
            this,
            "see(message #%d): request submitted",
            message.number()
        );
    }

}
