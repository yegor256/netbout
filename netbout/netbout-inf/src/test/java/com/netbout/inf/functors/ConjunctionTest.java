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
import com.netbout.inf.FolderMocker;
import com.netbout.inf.Functor;
import com.netbout.inf.Ray;
import com.netbout.inf.Term;
import com.netbout.inf.TermMocker;
import com.netbout.inf.atoms.PredicateAtom;
import com.netbout.inf.ray.MemRay;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case of {@link Conjunction}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class ConjunctionTest {

    /**
     * Conjunction can find a msg from two sub-functors.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsMessageFromTwoSubFunctors() throws Exception {
        final Ray ray = new MemRay(new FolderMocker().mock().path());
        final long msg = new Random().nextLong();
        ray.msg(msg);
        final Conjunction functor = new Conjunction();
        final Term first = new TermMocker().shiftTo(msg).mock();
        final Term second = new TermMocker().shiftTo(msg).mock();
        final Term term = functor.build(
            ray,
            Arrays.asList(
                new Atom[] {
                    new PredicateAtom(
                        "first",
                        Arrays.asList(new Atom[0]),
                        new Functor() {
                            @Override
                            public Term build(final Ray ray,
                                final List<Atom> atoms) {
                                return first;
                            }
                        }
                    ),
                    new PredicateAtom(
                        "second",
                        Arrays.asList(new Atom[0]),
                        new Functor() {
                            @Override
                            public Term build(final Ray ray,
                                final List<Atom> atoms) {
                                return second;
                            }
                        }
                    ),
                }
            )
        );
        MatcherAssert.assertThat(
            ray.cursor().shift(term).msg().number(),
            Matchers.equalTo(msg)
        );
        Mockito.verify(first).shift(Mockito.any(Cursor.class));
        Mockito.verify(second).shift(Mockito.any(Cursor.class));
    }

}
