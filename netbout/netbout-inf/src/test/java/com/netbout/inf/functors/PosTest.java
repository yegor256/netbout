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
import com.netbout.inf.MsgMocker;
import com.netbout.inf.Ray;
import com.netbout.inf.Term;
import com.netbout.inf.atoms.NumberAtom;
import com.netbout.inf.ray.MemRay;
import java.util.Arrays;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link Pos}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class PosTest {

    /**
     * Pos can catch the first message in a row.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void catchesTheFirstMessage() throws Exception {
        final Ray ray = new MemRay(new FolderMocker().mock().path());
        final long msg = MsgMocker.number();
        ray.msg(msg);
        final Pos functor = new Pos();
        final Term term = functor.build(
            ray,
            Arrays.<Atom<?>>asList(new NumberAtom(0L))
        );
        Cursor cursor = ray.cursor().shift(term);
        MatcherAssert.assertThat(
            cursor.msg().number(),
            Matchers.equalTo(msg)
        );
        cursor = cursor.shift(term);
        MatcherAssert.assertThat(cursor.end(), Matchers.is(true));
    }

    /**
     * Pos can catch the second one.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void catchesTheSecondMessage() throws Exception {
        final Ray ray = new MemRay(new FolderMocker().mock().path());
        final long msg = MsgMocker.number();
        ray.msg(msg + 1);
        ray.msg(msg);
        final Pos functor = new Pos();
        final Term term = functor.build(
            ray,
            Arrays.<Atom<?>>asList(new NumberAtom(1L))
        );
        Cursor cursor = ray.cursor().shift(term);
        MatcherAssert.assertThat(
            cursor.msg().number(),
            Matchers.equalTo(msg)
        );
        cursor = cursor.shift(term);
        MatcherAssert.assertThat(cursor.end(), Matchers.is(true));
    }

}