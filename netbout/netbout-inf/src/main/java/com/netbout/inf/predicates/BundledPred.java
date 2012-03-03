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
package com.netbout.inf.predicates;

import com.netbout.inf.Atom;
import com.netbout.inf.Meta;
import com.netbout.inf.PredicateException;
import com.netbout.spi.Message;
import com.netbout.spi.Participant;
import com.netbout.spi.Urn;
import com.ymock.util.Logger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Allows only bundled messages.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Meta(name = "bundled", extracts = true)
public final class BundledPred extends AbstractVarargPred {

    /**
     * Cached messages and their markers.
     */
    private static final ConcurrentMap<Long, String> MARKERS =
        new ConcurrentHashMap<Long, String>();

    /**
     * Cached bouts and their message numbers.
     */
    private static final ConcurrentMap<Long, Long> BOUTS =
        new ConcurrentHashMap<Long, Long>();

    /**
     * Cached message numbers and bout numbers.
     */
    private static final ConcurrentMap<Long, Long> MESSAGES =
        new ConcurrentHashMap<Long, Long>();

    /**
     * List of already passed bundles.
     */
    private final transient Set<String> passed = new HashSet<String>();

    /**
     * Public ctor.
     * @param args The arguments
     */
    public BundledPred(final List<Atom> args) {
        super(args);
    }

    /**
     * Extracts necessary data from message.
     * @param msg The message to extract from
     */
    public static void extract(final Message msg) {
        final Set<Urn> names = new TreeSet<Urn>();
        for (Participant dude : msg.bout().participants()) {
            names.add(dude.identity().name());
        }
        BundledPred.MARKERS.put(msg.number(), Logger.format("%[list]s", names));
        BundledPred.BOUTS.put(msg.bout().number(), msg.number());
        BundledPred.MESSAGES.put(msg.number(), msg.bout().number());
    }

    /**
     * Get marker for message.
     * @param msg The message to extract from
     * @return The marker
     */
    public static String marker(final Long msg) {
        if (!BundledPred.MARKERS.containsKey(msg)) {
            throw new IllegalArgumentException(
                String.format("marker not found for message #%d", msg)
            );
        }
        return BundledPred.MARKERS.get(msg);
    }

    /**
     * Get bout number by message.
     * @param bout Number of message
     * @return The bout number
     */
    public static Long boutOf(final Long msg) {
        if (!BundledPred.MESSAGES.containsKey(msg)) {
            throw new IllegalArgumentException(
                String.format("bout not found for message #%d", msg)
            );
        }
        return BundledPred.MESSAGES.get(msg);
    }

    /**
     * Get marker for bout number.
     * @param bout Number of bout
     * @return The marker
     */
    public static String markerOfBout(final Long bout) {
        if (!BundledPred.BOUTS.containsKey(bout)) {
            throw new IllegalArgumentException(
                String.format("bout #%d not found", bout)
            );
        }
        return BundledPred.marker(BundledPred.BOUTS.get(bout));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long next() {
        throw new PredicateException("BUNDLED#next()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        throw new PredicateException("BUNDLED#hasNext()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final Long message) {
        final String marker = this.MARKERS.get(message);
        boolean allow;
        if (this.passed.contains(marker)) {
            allow = false;
        } else {
            this.passed.add(marker);
            allow = true;
        }
        return allow;
    }

}
