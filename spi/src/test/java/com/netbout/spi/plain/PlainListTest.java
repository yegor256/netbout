/**
 * Copyright (c) 2009-2012, Netbout.com
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
package com.netbout.spi.plain;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Test case for {@link PlainList}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@RunWith(Parameterized.class)
public final class PlainListTest {

    /**
     * The data to test against.
     */
    private final transient List<?> list;

    /**
     * Public ctor.
     * @param lst The list
     */
    public PlainListTest(final List<?> lst) {
        this.list = lst;
    }

    /**
     * Incoming params.
     * @return The collection of them
     * @throws Exception If some problem inside
     */
    @Parameterized.Parameters
    public static Collection<Object[]> parameters() throws Exception {
        return Arrays.asList(
            new Object[][] {
                new Object[] {Arrays.asList(new Long[]{})},
                new Object[] {Arrays.asList(new Long[]{1L, 2L})},
                new Object[] {Arrays.asList(new Date[]{new Date()})},
                new Object[] {Arrays.asList(new Boolean[]{true, false})},
                new Object[] {Arrays.asList(new String[]{"abc", "cde;"})},
                new Object[] {
                    Arrays.asList(
                        new String[]{
                            "\u0443\u0440\u0430!",
                            "\u0440\u0430\u0431\u043E\u0442\u0430\u0435\u0442!",
                        }
                    ),
                },
            }
        );
    }

    /**
     * PlainList can convert lists to texts and back.
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void convertsInBothWays() throws Exception {
        final String sep = ", ";
        final String original = StringUtils.join(this.list, sep);
        final PlainList plain = new PlainList(this.list);
        final String text = plain.toString();
        final List reverse = PlainList.valueOf(text).value();
        MatcherAssert.assertThat(
            StringUtils.join(reverse, sep),
            Matchers.equalTo(original)
        );
    }

}
