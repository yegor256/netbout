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
package com.netbout.inf.lattice;

import com.netbout.inf.Cursor;
import com.netbout.inf.CursorMocker;
import com.netbout.inf.Lattice;
import com.netbout.inf.MsgMocker;
import java.util.Arrays;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link LatticeBuilder}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class LatticeBuilderTest {

    /**
     * LatticeBuilder can create lattice from numbers.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void createsLatticeFromNumbers() throws Exception {
        final SortedSet<Long> numbers = this.numbers();
        final LatticeBuilder builder = new LatticeBuilder();
        for (Long number : numbers) {
            builder.set(number, true, numbers);
        }
        MatcherAssert.assertThat(
            new LatticeBuilder().fill(numbers).build(),
            Matchers.equalTo(builder.build())
        );
    }

    /**
     * LatticeBuilder can copy lattice.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void copiesLatticeWithAllNumbers() throws Exception {
        final Lattice lattice = new LatticeBuilder()
            .fill(this.numbers())
            .build();
        MatcherAssert.assertThat(
            lattice,
            Matchers.equalTo(new LatticeBuilder().copy(lattice).build())
        );
    }

    /**
     * Create some numbers.
     * @return Set of numbers
     */
    private SortedSet<Long> numbers() {
        final SortedSet<Long> numbers =
            new TreeSet<Long>(Collections.reverseOrder());
        // @checkstyle MagicNumber (1 line)
        for (int num = 0; num < 10; ++num) {
            numbers.add(MsgMocker.number());
        }
        return numbers;
    }

}
