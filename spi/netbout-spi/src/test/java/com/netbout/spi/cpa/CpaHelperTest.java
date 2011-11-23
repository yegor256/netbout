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
package com.netbout.spi.cpa;

import com.netbout.spi.Helper;
import com.netbout.spi.HelperException;
import com.netbout.spi.Identity;
import com.netbout.spi.Token;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link CpaHelper}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class CpaHelperTest {

    /**
     * The helper.
     */
    private transient Helper helper;

    /**
     * Prepare the helper to work with.
     * @throws Exception If there is some problem inside
     */
    @Before
    public void prepare() throws Exception {
        this.helper = new CpaHelper(this.getClass().getPackage().getName());
        final Identity identity = Mockito.mock(Identity.class);
        this.helper.init(identity);
    }

    /**
     * Helper discovers farms and operations in classpatch.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testDiscoveryOfOperations() throws Exception {
        MatcherAssert.assertThat(
            this.helper.supports().size(),
            Matchers.greaterThan(0)
        );
    }

    /**
     * Test with different types of params.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testDifferentTypesOfParams() throws Exception {
        final Token token = Mockito.mock(Token.class);
        Mockito.doReturn("comparison").when(token).mnemo();
        Mockito.doReturn("alpha-12").when(token).arg(0);
        Mockito.doReturn("6").when(token).arg(1);
        this.helper.execute(token);
        Mockito.verify(token).result("false");
    }

    /**
     * Test with NULL response.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testNullResponse() throws Exception {
        final Token token = Mockito.mock(Token.class);
        Mockito.doReturn("empty").when(token).mnemo();
        this.helper.execute(token);
        Mockito.verify(token).result("NULL");
    }

    /**
     * Test with lists.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testLists() throws Exception {
        final Token token = Mockito.mock(Token.class);
        Mockito.doReturn("list").when(token).mnemo();
        Mockito.doReturn("4").when(token).arg(0);
        this.helper.execute(token);
        Mockito.verify(token).result(Mockito.anyString());
    }

    /**
     * Test with text lists.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testTextLists() throws Exception {
        final Token token = Mockito.mock(Token.class);
        Mockito.doReturn("texts").when(token).mnemo();
        this.helper.execute(token);
        Mockito.verify(token).result("byBuIGU=,InR3byI=");
    }

    /**
     * Helper can't be used without a call to {@link Helper#init()}.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = HelperException.class)
    public void testSupportsWithoutInit() throws Exception {
        new CpaHelper(this.getClass().getPackage().getName()).supports();
    }

    /**
     * Helper can't execute unknown operation.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = HelperException.class)
    public void testCallToUnknownOperation() throws Exception {
        final Token token = Mockito.mock(Token.class);
        Mockito.doReturn("unknown-operation").when(token).mnemo();
        this.helper.execute(token);
    }

    /**
     * Sample farm.
     */
    @Farm
    public static final class SampleFarm implements IdentityAware {
        /**
         * {@inheritDoc}
         */
        @Override
        public void init(final Identity identity) {
            MatcherAssert.assertThat(identity, Matchers.notNullValue());
        }
        /**
         * Sample operation.
         * @param text The text to translate
         * @param len Length to compare with
         * @return The translated text
         */
        @Operation("comparison")
        public Boolean longerThan(final String text, final Long len) {
            return text.length() > len;
        }
        /**
         * Empty operation with no result and no args.
         */
        @Operation("empty")
        public void empty() {
            // intentionally empty
        }
        /**
         * List as output.
         * @param size Size of the list to return
         * @return The list just created
         */
        @Operation("list")
        public Long[] list(final Long size) {
            final Long[] list = new Long[size.intValue()];
            final Random random = new Random();
            for (int pos = 0; pos < size; pos += 1) {
                list[pos] = random.nextLong();
            }
            return list;
        }
        /**
         * List of texts.
         * @return The list just created
         */
        @Operation("texts")
        public String[] texts() {
            final String[] list = new String[2];
            // @checkstyle MultipleStringLiterals (1 line)
            list[0] = "o n e";
            list[1] = "\"two\"";
            return list;
        }
        /**
         * Return back the same message as being sent.
         * @param msg The message
         * @return The same message
         */
        @Operation("echo")
        public String echo(final String msg) {
            return msg;
        }
    }

}
