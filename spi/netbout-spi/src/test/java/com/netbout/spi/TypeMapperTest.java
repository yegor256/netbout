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
import java.util.Collection;
import java.util.Date;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link TypeMapper}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class TypeMapperTest {

    /**
     * Let's test object to text convertions.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testBothWaysConversion() throws Exception {
        final Collection<Triple> triples = new ArrayList<Triple>();
        triples.add(new Triple("NULL", null, String.class));
        triples.add(new Triple("dGVzdCBtZQ==", "test me", String.class));
        // @checkstyle MagicNumber (1 line)
        triples.add(new Triple("-76", new Long(-76L), Long.class));
        triples.add(new Triple("true", Boolean.TRUE, Boolean.class));
        triples.add(new Triple("false", Boolean.FALSE, Boolean.class));
        triples.add(new Triple("1,2", new Long[] {1L, 2L}, Long[].class));
        triples.add(
            new Triple("YQ==,Yg==", new String[] {"a", "b"}, String[].class)
        );
        final Date date = new Date();
        triples.add(
            new Triple(
                "2009-02-13T23:31:30.123Z",
                // @checkstyle MagicNumber (1 line)
                new Date(1234567890123L),
                Date.class
            )
        );
        for (Triple triple : triples) {
            triple.validate();
        }
    }

    private static final class Triple {
        /**
         * The text.
         */
        private final String text;
        /**
         * The object.
         */
        private final Object object;
        /**
         * The type.
         */
        private final Class type;
        /**
         * Public ctor.
         * @param txt The text
         * @param obj The object
         * @param tpe The type
         */
        public Triple(final String txt, final Object obj, final Class tpe) {
            this.text = txt;
            this.object = obj;
            this.type = tpe;
        }
        /**
         * Validate this triple.
         * @throws HelperException If some problem inside
         */
        public void validate() throws HelperException {
            MatcherAssert.assertThat(
                TypeMapper.toText(this.object),
                Matchers.equalTo(this.text)
            );
            MatcherAssert.assertThat(
                TypeMapper.toObject(this.text, this.type),
                Matchers.equalTo(this.object)
            );
        }
    }

}
