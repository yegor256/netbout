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
package com.netbout.inf.ray;

import com.netbout.inf.Cursor;
import com.netbout.inf.CursorMocker;
import com.netbout.inf.Term;
import com.netbout.inf.TermBuilder;
import java.util.Arrays;
import java.util.Random;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link AndTerm}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class AndTermTest {

    /**
     * AndTerm can shift a cursor.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void shiftsCursorToTheFirstValue() throws Exception {
        final IndexMap map = new DefaultIndexMap();
        final String attr = "attribute name";
        final String first = "some text-1 \u0433!";
        final String second = "some text-2 \u0433!";
        final String third = "some text-3 \u0433!";
        final long msg = new Random().nextLong();
        map.index(attr).add(msg + 1, first);
        map.index(attr).add(msg, first);
        map.index(attr).add(msg, second);
        map.index(attr).add(msg, third);
        map.index(attr).add(msg - 1, second);
        map.index(attr).add(msg - 2, third);
        final Term term = new AndTerm(
            map,
            Arrays.asList(
                new Term[] {
                    new MatcherTerm(map, attr, first),
                    new MatcherTerm(map, attr, second),
                    new MatcherTerm(map, attr, third),
                }
            )
        );
        final Cursor cursor = new MemCursor(Long.MAX_VALUE, map);
        MatcherAssert.assertThat(
            term.shift(cursor).msg().number(),
            Matchers.equalTo(msg)
        );
        MatcherAssert.assertThat(
            term.shift(term.shift(cursor)).end(),
            Matchers.equalTo(true)
        );
    }

}
