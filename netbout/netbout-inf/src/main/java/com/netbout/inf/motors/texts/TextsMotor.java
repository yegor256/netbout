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
package com.netbout.inf.motors.texts;

import com.netbout.inf.Atom;
import com.netbout.inf.Pointer;
import com.netbout.inf.Predicate;
import com.netbout.inf.PredicateException;
import com.netbout.inf.Store;
import com.netbout.inf.atoms.TextAtom;
import com.netbout.inf.atoms.VariableAtom;
import com.netbout.inf.motors.StoreAware;
import com.netbout.inf.triples.HsqlTriples;
import com.netbout.inf.triples.Triples;
import com.netbout.spi.Message;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

/**
 * Texts motor.
 *
 * <p>This class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class TextsMotor implements Pointer, StoreAware {

    /**
     * Message number to word, in text (name of triple).
     */
    @SuppressWarnings("PMD.DefaultPackage")
    static final String MSG_TEXT_TO_WORD = "message-text-to-word";

    /**
     * Bout number to word, in bout title (name of triple).
     */
    @SuppressWarnings("PMD.DefaultPackage")
    static final String TITLE_TO_WORD = "bout-title-to-word";

    /**
     * Message to bout (name of triple).
     */
    @SuppressWarnings("PMD.DefaultPackage")
    static final String MSG_TO_BOUT = "message-to-bout";

    /**
     * The store.
     */
    private transient Store store;

    /**
     * The triples.
     */
    private final transient Triples triples;

    /**
     * Public ctor.
     * @param dir The directory to work in
     */
    public TextsMotor(final File dir) {
        this.triples = new HsqlTriples(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStore(final Store str) {
        this.store = str;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String statistics() {
        return this.getClass().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "TextsMotor";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws java.io.IOException {
        this.triples.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean pointsTo(final String name) {
        return name.matches("matches");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Predicate build(final String name, final List<Atom> atoms) {
        Predicate predicate;
        final Set<String> words = this.words(((TextAtom) atoms.get(0)).value());
        if (words.size() > 1) {
            final List<Atom> terms = new ArrayList<Atom>(words.size());
            for (String word : words) {
                terms.add(
                    this.build(
                        name,
                        Arrays.asList(
                            new Atom[] {
                                new TextAtom(word),
                                atoms.get(1),
                            }
                        )
                    )
                );
            }
            predicate = this.store.build("and", terms);
        } else if (words.isEmpty()) {
            predicate = this.store.build("true", Arrays.asList(new Atom[]{}));
        } else {
            final VariableAtom var = (VariableAtom) atoms.get(1);
            final String word = words.iterator().next();
            if (var.equals(VariableAtom.TEXT)) {
                predicate = new MatchesTextPred(this.triples, word);
            } else if (var.equals(VariableAtom.BOUT_TITLE)) {
                predicate = new MatchesTitlePred(this.triples, word);
            } else {
                throw new PredicateException(
                    String.format("Variable %s not supported in MATCHES", var)
                );
            }
        }
        return predicate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void see(final Message msg) {
        for (String word : TextsMotor.words(msg.text())) {
            this.triples.put(
                msg.number(),
                TextsMotor.MSG_TEXT_TO_WORD,
                word
            );
        }
        this.triples.put(
            msg.number(),
            TextsMotor.MSG_TO_BOUT,
            msg.bout().number().toString()
        );
        this.triples.clear(msg.bout().number(), TextsMotor.TITLE_TO_WORD);
        for (String word : TextsMotor.words(msg.bout().title())) {
            this.triples.put(
                msg.bout().number(),
                TextsMotor.TITLE_TO_WORD,
                word
            );
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

}
