/**
 * Copyright (c) 2009-2011, netBout.com
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
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import com.netbout.spi.Participant;
import com.netbout.spi.Urn;
import com.ymock.util.Logger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
     * The heap.
     */
    private final transient Heap heap = new Heap();

    /**
     * Multiplexer of tasks.
     */
    private final transient Mux mux = new Mux();

    /**
     * Public ctor.
     * @param ibus The BUS to work with
     */
    public DefaultInfinity(final Bus ibus) {
        this.bus = ibus;
        Logger.info(this, "#DefaultInfinity(%[type]s): instantiated", ibus);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        Logger.info(this, "#close(): will stop Mux in a second");
        this.mux.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long eta(final Urn who) {
        return this.mux.eta(who);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Long> bouts(final String query) {
        return new LazyBouts(this.heap, this.messages(query));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Long> messages(final String query) {
        return new LazyMessages(
            this.heap.messages(),
            new PredicateBuilder(this.bus).parse(query)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void see(final Identity identity) {
        this.mux.submit(
            new HashSet(Arrays.asList(new Urn[] {identity.name()})),
            new SeeIdentityTask(this, this.bus, identity)
        );
        Logger.debug(
            this,
            "see('%s'): request submitted",
            identity.name()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void see(final Bout bout) {
        this.mux.submit(
            this.names(bout),
            new SeeBoutTask(this, this.bus, bout)
        );
        Logger.debug(
            this,
            "see(bout #%d): request submitted",
            bout.number()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void see(final Message message) {
        this.mux.submit(
            this.names(message.bout()),
            new SeeMessageTask(this.heap, message)
        );
        Logger.debug(
            this,
            "see(message #%d): request submitted",
            message.number()
        );
    }

    /**
     * Names of bout participants.
     * @param bout The bout
     * @return Names
     */
    private static Set<Urn> names(final Bout bout) {
        final Set<Urn> names = new HashSet<Urn>();
        for (Participant dude : bout.participants()) {
            names.add(dude.identity().name());
        }
        return names;
    }

}
