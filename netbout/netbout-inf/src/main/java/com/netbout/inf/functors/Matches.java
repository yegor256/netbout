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
package com.netbout.inf.functors;

import com.netbout.inf.Atom;
import com.netbout.inf.Cursor;
import com.netbout.inf.Functor;
import com.netbout.inf.Msg;
import com.netbout.inf.Ray;
import com.netbout.inf.Term;
import com.netbout.inf.atoms.TextAtom;
import com.netbout.inf.notices.MessagePostedNotice;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

/**
 * Allows only matched messages.
 *
 * <p>This class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@NamedAs("matches")
final class Matches implements Functor {

    /**
     * The attribute to use.
     */
    private static final String ATTR = "matches";

    /**
     * {@inheritDoc}
     */
    @Override
    public final Term build(final Ray ray, final List<Atom> atoms) {
        final Set<String> words = Matches.words(
            TextAtom.class.cast(atoms.get(0)).value()
        );
        final Collection<Term> terms = new ArrayList<Term>(words.size());
        for (String word : words) {
            terms.add(ray.builder().matcher(Matches.ATTR, word));
        }
        return ray.builder().or(terms);
    }

    /**
     * Notice when new message is posted.
     * @param ray The ray
     * @param notice The notice
     */
    @Noticable
    public void see(final Ray ray, final MessagePostedNotice notice) {
        final Msg msg = ray.msg(notice.message().number());
        for (String word : Matches.words(notice.message().text())) {
            msg.add(Matches.ATTR, word);
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
