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
package com.netbout.inf.predicates.text;

import com.netbout.inf.Atom;
import com.netbout.inf.Meta;
import com.netbout.inf.Predicate;
import com.netbout.inf.atoms.TextAtom;
import com.netbout.inf.atoms.VariableAtom;
import com.netbout.inf.predicates.AbstractVarargPred;
import com.netbout.inf.predicates.FalsePred;
import com.netbout.inf.predicates.logic.AndPred;
import com.netbout.spi.Message;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Matches text against search string.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Meta(name = "matches", extracts = true)
public final class MatchesPred extends AbstractVarargPred {

    /**
     * Cached messages and their namespaces.
     * @checkstyle LineLength (3 lines)
     */
    public static final ConcurrentMap<VariableAtom, ConcurrentMap<String, SortedSet<Long>>> CACHE =
        new ConcurrentHashMap<VariableAtom, ConcurrentMap<String, SortedSet<Long>>>();

    /**
     * Compound predicate.
     */
    public final transient Predicate predicate;

    /**
     * Public ctor.
     * @param args The arguments
     */
    public MatchesPred(final List<Atom> args) {
        super(args);
        final Set<String> words = this.words(this.arg(0).value().toString());
        if (words.size() > 1) {
            final List<Atom> atoms = new ArrayList<Atom>(words.size());
            for (String word : words) {
                atoms.add(
                    new MatchesPred(
                        Arrays.asList(
                            new Atom[] {
                                new TextAtom(word),
                                this.arg(1),
                            }
                        )
                    )
                );
            }
            this.predicate = new AndPred(atoms);
        } else {
            if (this.CACHE.containsKey(this.arg(1))
                && this.CACHE.get(this.arg(1)).containsKey(this.arg(0))) {
                predicate = new MatchingPred(
                    this.CACHE.get(this.arg(1)).get(this.arg(0))
                );
            } else {
                predicate = new FalsePred();
            }
        }
    }

    /**
     * Extracts necessary data from message.
     * @param from The message to extract from
     * @param msg Where to extract
     */
    public static void extract(final Message from) {
        MatchesPred.extract("text", from.text(), from.number());
        MatchesPred.extract("bout.title", from.text(), from.number());
        MatchesPred.extract("author.alias", from.text(), from.number());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long next() {
        return this.predicate.next();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        return this.predicate.hasNext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final Long message) {
        return this.predicate.contains(message);
    }

    /**
     * Extracts necessary data from message.
     * @param name Variable name
     * @param text The text
     * @param msg Message number
     */
    private static void extract(final String name, final String text,
        final Long msg) {
        final VariableAtom var = new VariableAtom(name);
        for (String word : MatchesPred.words(text)) {
            MatchesPred.CACHE.putIfAbsent(
                var,
                new ConcurrentHashMap<String, SortedSet<Long>>()
            );
            MatchesPred.CACHE.get(var).putIfAbsent(
                word,
                new ConcurrentSkipListSet<Long>(Collections.reverseOrder())
            );
            MatchesPred.CACHE.get(var).get(word).add(msg);
        }
    }

    /**
     * Extract words from text.
     * @param text The text
     * @return Set of words
     */
    private static Set<String> words(final String text) {
        return new HashSet<String>(
            Arrays.asList(
                text.replaceAll(
                    "['\"\\!@#\\$%\\?\\^&\\*\\(\\),\\.\\[\\]=\\+\\/]+",
                    "  "
                ).trim()
                    .toUpperCase(Locale.ENGLISH)
                    .split("\\s+")
            )
        );
    }

    private static final class MatchingPred implements Predicate {
        /**
         * Found set of message numbers.
         */
        public final transient SortedSet<Long> messages;
        /**
         * Iterator of them.
         */
        public final transient Iterator<Long> iterator;
        /**
         * Public ctor.
         * @param msgs Set of messages
         */
        public MatchingPred(final SortedSet<Long> msgs) {
            this.messages = msgs;
            this.iterator = msgs.iterator();
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
         * {@inheritDoc}
         */
        @Override
        public String value() {
            throw new IllegalStateException("#value()");
        }
    }

}
