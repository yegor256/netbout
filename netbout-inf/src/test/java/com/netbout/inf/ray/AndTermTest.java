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
 * this code accidentally and without intent to use it, please report this
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

import com.netbout.inf.Attribute;
import com.netbout.inf.Cursor;
import com.netbout.inf.MsgMocker;
import com.netbout.inf.Term;
import com.netbout.inf.ray.imap.DefaultIndexMap;
import java.util.Arrays;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case of {@link AndTerm}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class AndTermTest {

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * AndTerm can shift a cursor.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void shiftsCursorToTheFirstValue() throws Exception {
        final IndexMap map = new DefaultIndexMap(
            this.temp.newFolder("foo")
        );
        final Attribute attr = new Attribute("attribute-name");
        final String first = "some text-1 \u0433!";
        final String second = "some text-2 \u0433!";
        final String third = "some text-3 \u0433!";
        final long msg = MsgMocker.number();
        map.index(attr).add(msg + 1, first);
        map.index(attr).add(msg, first);
        map.index(attr).add(msg, second);
        map.index(attr).add(msg, third);
        map.index(attr).add(msg - 1, second);
        map.index(attr).add(msg - 2, third);
        Cursor cursor = new MemCursor(Long.MAX_VALUE, map).shift(
            new AndTerm(
                map,
                Arrays.<Term>asList(
                    new Valve(new MatcherTerm(map, attr, first)),
                    new Valve(new MatcherTerm(map, attr, second)),
                    new Valve(new MatcherTerm(map, attr, third))
                )
            )
        );
        MatcherAssert.assertThat(cursor.msg().number(), Matchers.equalTo(msg));
        cursor = cursor.shift(
            new AndTerm(
                map,
                Arrays.<Term>asList(
                    new Valve(new MatcherTerm(map, attr, first)),
                    new Valve(new MatcherTerm(map, attr, second)),
                    new Valve(new MatcherTerm(map, attr, third))
                )
            )
        );
        MatcherAssert.assertThat(cursor.end(), Matchers.is(true));
    }

}
