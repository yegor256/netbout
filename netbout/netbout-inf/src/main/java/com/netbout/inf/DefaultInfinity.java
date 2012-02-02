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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;

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
     * All messages.
     */
    private final transient SortedMap<Long, Msg> all =
        new ConcurrentSkipListMap<Long, Msg>(Collections.<Long>reverseOrder());

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
    public List<Bundle> bundles(final String query) {
        throw new UnsupportedOperationException("#bundles()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Long> bouts(final String query) {
        final Set<Long> numbers = new TreeSet<Long>(
            Collections.<Long>reverseOrder()
        );
        for (Long msg : this.messages(query)) {
            numbers.add(this.all.get(msg).bout());
        }
        Logger.debug(
            this,
            "#bouts(\"%s\"): found %d bouts: %[list]s",
            query,
            numbers.size(),
            numbers
        );
        return new ArrayList<Long>(numbers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Long> messages(final String query) {
        final Predicate predicate = new PredicateBuilder(this.bus).parse(query);
        final List<Long> numbers = new ArrayList<Long>();
        int pos = 0;
        for (Msg msg : this.all.values()) {
            if ((Boolean) predicate.evaluate(msg, pos)) {
                numbers.add(msg.number());
                pos += 1;
            }
        }
        Logger.debug(
            this,
            "#messages(\"%s\"): found %d messages: %[list]s",
            query,
            numbers.size(),
            numbers
        );
        return numbers;
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
        Logger.info(
            this,
            "#see(bout #%d): cached %d messages of bout #%d in %dms",
            bout.number(),
            numbers.size(),
            bout.number(),
            System.currentTimeMillis() - start
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void see(final Message message) {
        final Long number = message.number();
        final MsgBuilder builder = new MsgBuilder(message);
        this.all.put(number, builder.build());
        this.all.put(number, builder.rebuild(this.all.get(number)));
    }

}
