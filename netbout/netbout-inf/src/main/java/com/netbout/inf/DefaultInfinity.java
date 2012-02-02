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
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of Infitity.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
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
     * Public ctor.
     * @param ibus The BUS to work with
     */
    public DefaultInfinity(final Bus ibus) {
        this.bus = ibus;
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
        final long start = System.currentTimeMillis();
        final List<Long> numbers = this.bus
            .make("get-bouts-of-identity")
            .synchronously()
            .arg(identity.name())
            .asDefault(new ArrayList<Long>())
            .exec();
        for (Long number : numbers) {
            try {
                this.see(identity.bout(number));
            } catch (com.netbout.spi.BoutNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
        }
        Logger.info(
            this,
            "#see(%s): cached %d bouts of '%s' in %dms",
            identity.name(),
            numbers.size(),
            identity.name(),
            System.currentTimeMillis() - start
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void see(final Bout bout) {
        final long start = System.currentTimeMillis();
        final List<Long> numbers = this.bus
            .make("get-bout-messages")
            .synchronously()
            .arg(bout.number())
            .asDefault(new ArrayList<Long>())
            .exec();
        for (Long number : numbers) {
            try {
                this.see(bout.message(number));
            } catch (com.netbout.spi.MessageNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
        }
        if (!numbers.isEmpty()) {
            Logger.info(
                this,
                "#see(bout #%d): cached %d messages of bout #%d in %dms",
                bout.number(),
                numbers.size(),
                bout.number(),
                System.currentTimeMillis() - start
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void see(final Message message) {
        final Long number = message.number();
        final MsgBuilder builder = new MsgBuilder(message);
        this.heap.put(number, builder.build());
        this.heap.put(number, builder.rebuild(this.heap.get(number)));
    }

}
