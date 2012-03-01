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
package com.netbout.inf.predicates.logic;

import com.netbout.inf.Atom;
import com.netbout.inf.Predicate;
import com.netbout.inf.PredicateMocker;
import com.netbout.inf.atoms.NumberAtom;
import com.netbout.inf.atoms.TextAtom;
import com.netbout.inf.predicates.FalsePred;
import java.util.Arrays;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link OrPred}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class OrPredTest {

    /**
     * OrPred can merge two predicates togethere.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void mergesTwoPredicates() throws Exception {
        final Predicate first = new PredicateMocker()
            .withMessages(new Long[] {1L})
            .mock();
        final Predicate second = new PredicateMocker()
            .withMessages(new Long[] {2L})
            .mock();
        final Predicate merger = new OrPred(
            Arrays.asList(new Atom[] {first, second})
        );
        MatcherAssert.assertThat("has next", merger.hasNext());
        MatcherAssert.assertThat(merger.next(), Matchers.equalTo(1L));
        MatcherAssert.assertThat("still has next", merger.hasNext());
        MatcherAssert.assertThat(merger.next(), Matchers.equalTo(2L));
        MatcherAssert.assertThat("now it is empty", !merger.hasNext());
    }

    /**
     * OrPred can handle an empty predicates gracefully.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void mergesTwoEmptyPredicates() throws Exception {
        final Predicate merger = new AndPred(
            Arrays.asList(new Atom[] {new FalsePred(), new FalsePred()})
        );
        MatcherAssert.assertThat("row is empty", !merger.hasNext());
    }

}
