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
import com.netbout.inf.Predicate;
import com.netbout.inf.atoms.TextAtom;
import com.netbout.spi.Message;
import com.netbout.spi.Participant;
import com.netbout.spi.Urn;
import com.ymock.util.Logger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
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
     * Cached participants and their messages.
     */
    public static final ConcurrentMap<Urn, SortedSet<Long>> DUDES =
        new ConcurrentHashMap<Urn, SortedSet<Long>>();

    /**
     * Cached bouts and their participants.
     */
    public static final ConcurrentMap<Long, SortedSet<Urn>> BOUTS =
        new ConcurrentHashMap<Long, SortedSet<Urn>>();

    /**
     * Found set of message numbers.
     */
    public final transient Set<Long> messages;

    /**
     * Iterator of them.
     */
    public final transient Iterator<Long> iterator;

    /**
     * Public ctor.
     * @param args The arguments
     */
    public TalksWithPred(final List<Atom> args) {
        super(args);
        final Urn urn = Urn.create(((TextAtom) this.arg(0)).value());
        if (this.DUDES.containsKey(urn)) {
            this.messages = this.DUDES.get(urn);
        } else {
            this.messages = new ConcurrentSkipListSet<Long>();
        }
        this.iterator = this.messages.iterator();
    }

    /**
     * Extracts necessary data from message.
     * @param msg The message to extract from
     */
    public static void extract(final Message msg) {
        final Long bout = msg.bout().number();
        TalksWithPred.BOUTS.putIfAbsent(
            bout,
            new ConcurrentSkipListSet<Urn>()
        );
        final Set<Urn> names = new HashSet<Urn>();
        for (Participant dude : msg.bout().participants()) {
            final Urn name = dude.identity().name();
            names.add(name);
            TalksWithPred.DUDES.putIfAbsent(
                name,
                new ConcurrentSkipListSet<Long>(Collections.reverseOrder())
            );
            TalksWithPred.DUDES.get(name).add(msg.number());
            TalksWithPred.BOUTS.get(bout).add(name);
        }
        final Iterator<Urn> iterator = TalksWithPred.BOUTS.get(bout).iterator();
        while (iterator.hasNext()) {
            final Urn name = iterator.next();
            if (!names.contains(name)) {
                iterator.remove();
                TalksWithPred.DUDES.get(name).remove(msg.number());
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
