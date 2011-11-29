/**
 * Copyright (c) 2009-2011, NetBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Test case for {@link PlainBuilder}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@RunWith(Parameterized.class)
public final class PlainBuilderTest {

    /**
     * The data to test against.
     */
    private final Object data;

    /**
     * Incoming params.
     * @return The collection of them
     */
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        final Random random = new Random();
        return Arrays.asList(
            new Object[][] {
                new Object[] { random.nextLong() },
                new Object[] { "" },
                new Object[] { "a" },
                new Object[] { "some text: 8(&^%$,:;,\"/\\+ " },
                new Object[] { new Date() },
                new Object[] { new Date(8927563737L) },
                new Object[] { true },
                new Object[] { Boolean.FALSE },
                new Object[] {
                    Arrays.asList(
                        new Long[]{ random.nextLong(), random.nextLong() }
                    )
                },
                new Object[]{ Arrays.asList(new Boolean[]{ true, false }) },
                new Object[] {
                    Arrays.asList(
                        new String[]{ "some text", "another text;;;" }
                    )
                },
            }
        );
    }

    /**
     * Public ctor.
     * @param obj The object
     */
    public PlainBuilderTest(final Object obj) {
        this.data = obj;
    }

    /**
     * Let's test object to text convertions.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testBothWaysConversion() throws Exception {
        final Plain<?> plain = PlainBuilder.fromObject(this.data);
        final String text = plain.toString();
        MatcherAssert.assertThat(
            (Plain) PlainBuilder.fromText(text),
            Matchers.equalTo((Plain) plain)
        );
    }

}
