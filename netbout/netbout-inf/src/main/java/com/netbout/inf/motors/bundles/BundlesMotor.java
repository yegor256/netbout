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
package com.netbout.inf.motors.bundles;

import com.netbout.spi.Message;
import com.netbout.spi.NetboutUtils;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.List;
import org.reflections.Reflections;

/**
 * Bundles motor.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class XmlMotor implements Pointer {

    /**
     * The index.
     */
    private final transient Index index;

    /**
     * Public ctor.
     * @param idx The index to work with
     */
    public BundlesMotor(final Index idx) {
        this.index = idx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Bundles";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean pointsTo(final String name) {
        return name.matches("bundled|unbundled");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate build(final String name, final List<Atom> atoms) {
        Predicate pred;
        if ("bundled".equals(name)) {
            pred = new BundledPred(new MapBundler(this.index));
        } else if ("unbundled".equals(name)) {
            pred = new UnbundledPred(new Marker());
        }
        return pred;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void see(final Message msg) {
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
     * {@inheritDoc}
     */
    @Override
    public void see(final Bout bout) {
        // nothing to do here
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

}
