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
import com.netbout.inf.Index;
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
     * MAP ID.
     */
    private static final String MAP_MARKERS =
        String.format("%s:markers", BundledPred.class.getName());

    /**
     * MAP ID.
     */
    private static final String MAP_BOUTS =
        String.format("%s:bouts", BundledPred.class.getName());

    /**
     * MAP ID.
     */
    private static final String MAP_MESSAGES =
        String.format("%s:messages", BundledPred.class.getName());

    /**
     * Cached messages and their markers.
     */
    private final transient ConcurrentMap<Long, String> markers;

    /**
     * List of already passed bundles.
     */
    private final transient Set<String> passed = new HashSet<String>();

    /**
     * Public ctor.
     * @param args The arguments
     * @param index The index to use for searching
     */
    public BundledPred(final List<Atom> args, final Index index) {
        super(args, index);
        this.markers = index.get(BundledPred.MAP_MARKERS);
    }

    /**
     * Extracts necessary data from message.
     * @param msg The message to extract from
     * @param index The index to extract to
     */
    public static void extract(final Message msg, final Index index) {
        final Set<Urn> names = new TreeSet<Urn>();
        for (Participant dude : msg.bout().participants()) {
            names.add(dude.identity().name());
        }
        final ConcurrentMap<Long, String> markers =
            index.get(BundledPred.MAP_MARKERS);
        final ConcurrentMap<Long, Long> bouts =
            index.get(BundledPred.MAP_BOUTS);
        final ConcurrentMap<Long, Long> messages =
            index.get(BundledPred.MAP_MESSAGES);
        markers.put(msg.number(), Logger.format("%[list]s", names));
        bouts.put(msg.bout().number(), msg.number());
        messages.put(msg.number(), msg.bout().number());
    }

    /**
     * Get marker for message.
     * @param index The index to use for it
     * @param msg The message to extract from
     * @return The marker
     */
    public static String marker(final Index index, final Long msg) {
        final ConcurrentMap<Long, String> markers =
            index.get(BundledPred.MAP_MARKERS);
        if (!markers.containsKey(msg)) {
            throw new IllegalArgumentException(
                String.format("marker not found for message #%d", msg)
            );
        }
        return markers.get(msg);
    }

    /**
     * Get bout number by message.
     * @param index The index to use for it
     * @param msg Number of message
     * @return The bout number
     */
    public static Long boutOf(final Index index, final Long msg) {
        final ConcurrentMap<Long, Long> messages =
            index.get(BundledPred.MAP_MESSAGES);
        if (!messages.containsKey(msg)) {
            throw new IllegalArgumentException(
                String.format("bout not found for message #%d", msg)
            );
        }
        return messages.get(msg);
    }

    /**
     * Get marker for bout number.
     * @param index The index to use for it
     * @param bout Number of bout
     * @return The marker
     */
    public static String markerOfBout(final Index index, final Long bout) {
        final ConcurrentMap<Long, Long> bouts =
            index.get(BundledPred.MAP_BOUTS);
        if (!bouts.containsKey(bout)) {
            throw new IllegalArgumentException(
                String.format("bout #%d not found", bout)
            );
        }
        return BundledPred.marker(index, bouts.get(bout));
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
        final String marker = this.markers.get(message);
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
