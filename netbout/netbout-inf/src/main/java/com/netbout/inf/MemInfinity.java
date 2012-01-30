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
import com.netbout.inf.predicates.TruePred;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Searcher, which keeps all data in memory.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class MemInfinity implements Infinity {

    /**
     * The bus to work with.
     */
    private final transient Bus bus;

    /**
     * Public ctor.
     * @param ibus The BUS to work with
     */
    public MemInfinity(final Bus ibus) {
        this.bus = ibus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Bundle> bundles(final Identity identity,
        final Predicate predicate) {
        throw new UnsupportedOperationException("#bundles()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Long> bouts(final Identity identity,
        final Predicate predicate) {
        final List<Bout> bouts = new ArrayList<Bout>();
        final List<Long> numbers = this.bus
            .make("get-bouts-of-identity")
            .synchronously()
            .arg(identity.name())
            .asDefault(new ArrayList<Long>())
            .exec();
        for (Long num : numbers) {
            try {
                bouts.add(identity.bout(num));
            } catch (com.netbout.spi.BoutNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
        }
        Collections.sort(bouts, Collections.reverseOrder());
        final List<Long> result = new ArrayList<Long>();
        for (Bout bout : bouts) {
            boolean matches = false;
            if (this.messages(bout, predicate).isEmpty()) {
                matches = (Boolean) predicate.evaluate(
                    new StubMessage(bout),
                    0
                );
            } else {
                matches = true;
            }
            if (matches) {
                result.add(bout.number());
            }
        }
        Logger.debug(
            this,
            "#bouts('%s'): %d bouts found",
            predicate,
            result.size()
        );
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Long> messages(final Bout bout, final Predicate predicate) {
        final List<Long> numbers = this.bus
            .make("get-bout-messages")
            .synchronously()
            .arg(bout.number())
            .asDefault(new ArrayList<Long>())
            .exec();
        final List<Message> messages = new ArrayList<Message>();
        for (Long num : numbers) {
            try {
                messages.add(bout.message(num));
            } catch (com.netbout.spi.MessageNotFoundException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
        Collections.sort(messages, Collections.reverseOrder());
        final List<Long> result = new ArrayList<Long>();
        for (Message msg : messages) {
            boolean visible = true;
            if (predicate instanceof TruePred) {
                final Object response = predicate.evaluate(msg, result.size());
                if (response instanceof Boolean) {
                    visible = (Boolean) response;
                } else {
                    throw new IllegalArgumentException(
                        Logger.format(
                            "Can't understand %[type]s response from '%s'",
                            response,
                            predicate
                        )
                    );
                }
            }
            if (visible) {
                result.add(msg.number());
            }
        }
        Logger.debug(
            this,
            "#messages(#%d, '%s'): %d message(s) found",
            bout.number(),
            predicate,
            result.size()
        );
        return result;
    }

}
