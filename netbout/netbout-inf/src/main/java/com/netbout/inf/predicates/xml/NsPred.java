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
package com.netbout.inf.predicates.xml;

import com.netbout.inf.Atom;
import com.netbout.inf.Index;
import com.netbout.inf.Meta;
import com.netbout.inf.predicates.AbstractVarargPred;
import com.netbout.spi.Message;
import com.netbout.spi.Urn;
import com.netbout.spi.xml.DomParser;
import com.ymock.util.Logger;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Namespace predicate.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Meta(name = "ns", extracts = true)
public final class NsPred extends AbstractVarargPred {

    /**
     * Cached messages and their namespaces.
     */
    private static final ConcurrentMap<Urn, Set<Long>> CACHE =
        new ConcurrentHashMap<Urn, Set<Long>>();

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
    public NsPred(final List<Atom> args, final Index index) {
        super(args, index);
        final Urn namespace = Urn.create(this.arg(0).value().toString());
        if (this.CACHE.containsKey(namespace)) {
            this.messages = this.CACHE.get(namespace);
        } else {
            this.messages = new ConcurrentSkipListSet<Long>();
        }
        this.iterator = this.messages.iterator();
    }

    /**
     * Extracts necessary data from message.
     * @param from The message to extract from
     * @param index The index to extract to
     */
    public static void extract(final Message from, final Index index) {
        final DomParser parser = new DomParser(from.text());
        if (parser.isXml()) {
            Urn namespace;
            try {
                namespace = parser.namespace();
                NsPred.CACHE.putIfAbsent(
                    namespace,
                    new ConcurrentSkipListSet<Long>(Collections.reverseOrder())
                );
                NsPred.CACHE.get(namespace).add(from.number());
                Logger.debug(
                    NsPred.class,
                    "#extract(#%d, ..): namespace '%s' found",
                    from.number(),
                    namespace
                );
            } catch (com.netbout.spi.xml.DomValidationException ex) {
                Logger.warn(
                    NsPred.class,
                    "#extract(#%d, ..): %[exception]s",
                    from.number(),
                    ex
                );
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
