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
package com.netbout.inf.predicates.math;

import com.netbout.inf.Atom;
import com.netbout.inf.Index;
import com.netbout.inf.Meta;
import com.netbout.inf.atoms.NumberAtom;
import com.netbout.inf.atoms.TextAtom;
import com.netbout.inf.atoms.VariableAtom;
import com.netbout.inf.predicates.AbstractVarargPred;
import com.netbout.spi.Message;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * All arguments should be equal to each other.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Meta(name = "equal", extracts = true)
public final class EqualPred extends AbstractVarargPred {

    /**
     * Cached messages and their values.
     * @checkstyle LineLength (3 lines)
     */
    private static final ConcurrentMap<VariableAtom, ConcurrentMap<Atom, SortedSet<Long>>> CACHE =
        new ConcurrentHashMap<VariableAtom, ConcurrentMap<Atom, SortedSet<Long>>>();

    /**
     * Found set of message numbers.
     */
    private final transient SortedSet<Long> messages;

    /**
     * Iterator of them.
     */
    private final transient Iterator<Long> iterator;

    /**
     * Public ctor.
     * @param args The arguments
     * @param index The index to use for searching
     */
    public EqualPred(final List<Atom> args, final Index index) {
        super(args, index);
        final VariableAtom var = (VariableAtom) this.arg(0);
        if (this.CACHE.containsKey(var)
            && this.CACHE.get(var).containsKey(this.arg(1))) {
            this.messages = this.CACHE.get(var).get(this.arg(1));
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
    public static void extract(final Message msg, final Index index) {
        EqualPred.var(
            VariableAtom.BOUT_NUMBER,
            new NumberAtom(msg.bout().number())
        ).add(msg.number());
        EqualPred.var(
            VariableAtom.NUMBER,
            new NumberAtom(msg.number())
        ).add(msg.number());
        EqualPred.var(
            VariableAtom.AUTHOR_NAME,
            new TextAtom(msg.author().name())
        ).add(msg.number());
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

    /**
     * Get access to list of message numbers.
     * @param var The variable
     * @param atom Value of it
     * @return Set of numbers
     */
    private static Set<Long> var(final VariableAtom var, final Atom atom) {
        EqualPred.CACHE.putIfAbsent(
            var,
            new ConcurrentHashMap<Atom, SortedSet<Long>>()
        );
        EqualPred.CACHE.get(var).putIfAbsent(
            atom,
            new ConcurrentSkipListSet<Long>(Collections.reverseOrder())
        );
        return EqualPred.CACHE.get(var).get(atom);
    }

}
