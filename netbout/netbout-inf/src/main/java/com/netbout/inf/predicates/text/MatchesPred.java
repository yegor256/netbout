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
import com.netbout.inf.Index;
import com.netbout.inf.Meta;
import com.netbout.inf.Predicate;
import com.netbout.inf.atoms.TextAtom;
import com.netbout.inf.atoms.VariableAtom;
import com.netbout.inf.predicates.AbstractVarargPred;
import com.netbout.inf.predicates.FalsePred;
import com.netbout.inf.predicates.TruePred;
import com.netbout.inf.predicates.logic.AndPred;
import com.netbout.spi.Message;
import com.netbout.spi.NetboutUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import org.apache.commons.collections.CollectionUtils;

/**
 * Matches text against search string.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Meta(name = "matches", extracts = true)
@SuppressWarnings({
    "PMD.TooManyMethods", "PMD.AvoidInstantiatingObjectsInLoops"
})
public final class MatchesPred extends AbstractVarargPred {

    /**
     * MAP ID.
     */
    private static final String MAP =
        String.format("%s:%%s", MatchesPred.class.getName());

    /**
     * Compound predicate.
     */
    private final transient Predicate predicate;

    /**
     * Public ctor.
     * @param args The arguments
     * @param index The index to use for searching
     */
    public MatchesPred(final List<Atom> args, final Index index) {
        super(args, index);
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
                        ),
                        index
                    )
                );
            }
            this.predicate = new AndPred(atoms, index);
        } else if (words.isEmpty()) {
            this.predicate = new TruePred();
        } else {
            final ConcurrentMap<String, SortedSet<Long>> cache =
                index.get(String.format(MatchesPred.MAP, this.arg(1)));
            this.predicate = this.byWord(cache, words.iterator().next());
        }
    }

    /**
     * Extracts necessary data from message.
     * @param msg The message to extract from
     * @param index The index to extract to
     */
    public static void extract(final Message msg, final Index index) {
        MatchesPred.extract(
            index,
            VariableAtom.TEXT,
            msg.text(),
            msg.number()
        );
        MatchesPred.extract(
            index,
            VariableAtom.BOUT_TITLE,
            msg.bout().title(),
            msg.number()
        );
        MatchesPred.extract(
            index,
            VariableAtom.AUTHOR_ALIAS,
            NetboutUtils.aliasOf(msg.author()),
            msg.number()
        );
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
     * Create predicate by this word.
     * @param cache The cache to use
     * @param word The word
     * @return The predicate
     */
    private Predicate byWord(final ConcurrentMap<String, SortedSet<Long>> cache,
        final String word) {
        Predicate pred = null;
        if (cache.containsKey(word)) {
            pred = new MatchingPred(cache.get(word));
        } else {
            for (String keyword : cache.keySet()) {
                if (keyword.contains(word)) {
                    pred = new MatchingPred(cache.get(keyword));
                    break;
                }
            }
        }
        if (pred == null) {
            pred = new FalsePred();
        }
        return pred;
    }

    /**
     * Extracts necessary data from message.
     * @param index The index
     * @param var Variable
     * @param text The text
     * @param msg Message number
     * @checkstyle ParameterNumber (3 lines)
     */
    private static void extract(final Index index, final VariableAtom var,
        final String text, final Long msg) {
        final ConcurrentMap<String, SortedSet<Long>> cache =
            index.get(String.format(MatchesPred.MAP, var));
        for (String word : MatchesPred.words(text)) {
            cache.putIfAbsent(
                word,
                new ConcurrentSkipListSet<Long>(Collections.reverseOrder())
            );
            cache.get(word).add(msg);
        }
    }

    /**
     * Extract words from text.
     * @param text The text
     * @return Set of words
     */
    private static Set<String> words(final String text) {
        final Set<String> words = new HashSet<String>(
            Arrays.asList(
                text.replaceAll(
                    "['\"\\!@#\\$%\\?\\^&\\*\\(\\),\\.\\[\\]=\\+\\/]+",
                    "  "
                ).trim().toUpperCase(Locale.ENGLISH).split("\\s+")
            )
        );
        CollectionUtils.filter(
            words,
            new org.apache.commons.collections.Predicate() {
                @Override
                public boolean evaluate(final Object obj) {
                    return ((String) obj).length() > 2;
                }
            }
        );
        return words;
    }

    private static final class MatchingPred implements Predicate {
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
