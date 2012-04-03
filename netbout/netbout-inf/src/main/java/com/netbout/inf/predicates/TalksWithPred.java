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
package com.netbout.inf.predicates;

import com.netbout.inf.Atom;
import com.netbout.inf.Index;
import com.netbout.inf.Meta;
import com.netbout.inf.atoms.TextAtom;
import com.netbout.spi.Message;
import com.netbout.spi.Participant;
import com.netbout.spi.Urn;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * This participant is in the bout.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Meta(name = "talks-with", extracts = true)
public final class TalksWithPred extends AbstractVarargPred {

    /**
     * MAP ID.
     */
    private static final String MAP_DUDES =
        String.format("%s:dudes", TalksWithPred.class.getName());

    /**
     * MAP ID.
     */
    private static final String MAP_BOUTS =
        String.format("%s:bouts", TalksWithPred.class.getName());

    /**
     * Found set of message numbers.
     */
    private final transient Set<Long> messages;

    /**
     * Iterator of them.
     */
    private final transient Iterator<Long> iterator;

    /**
     * Public ctor.
     * @param args The arguments
     * @param index The index to use for searching
     */
    public TalksWithPred(final List<Atom> args, final Index index) {
        super(args, index);
        final ConcurrentMap<Urn, SortedSet<Long>> dudes =
            index.get(TalksWithPred.MAP_DUDES);
        final Urn urn = Urn.create(((TextAtom) this.arg(0)).value());
        if (dudes.containsKey(urn)) {
            this.messages = dudes.get(urn);
        } else {
            this.messages = new ConcurrentSkipListSet<Long>();
        }
        this.iterator = this.messages.iterator();
    }

    /**
     * Extracts necessary data from message.
     * @param msg The message to extract from
     * @param index The index to extract to
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public static void extract(final Message msg, final Index index) {
        final Long bout = msg.bout().number();
        final ConcurrentMap<Urn, SortedSet<Long>> dudes =
            index.get(TalksWithPred.MAP_DUDES);
        final ConcurrentMap<Long, SortedSet<Urn>> bouts =
            index.get(TalksWithPred.MAP_BOUTS);
        bouts.putIfAbsent(
            bout,
            new ConcurrentSkipListSet<Urn>()
        );
        final Set<Urn> names = new HashSet<Urn>();
        for (Participant dude : msg.bout().participants()) {
            final Urn name = dude.identity().name();
            names.add(name);
            dudes.putIfAbsent(
                name,
                new ConcurrentSkipListSet<Long>(Collections.reverseOrder())
            );
            dudes.get(name).add(msg.number());
            bouts.get(bout).add(name);
        }
        final Iterator<Urn> iterator = bouts.get(bout).iterator();
        while (iterator.hasNext()) {
            final Urn name = iterator.next();
            if (!names.contains(name)) {
                iterator.remove();
                dudes.get(name).remove(msg.number());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long next() {
        return this.iterator.next();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        return this.iterator.hasNext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final Long message) {
        return this.messages.contains(message);
    }

}
