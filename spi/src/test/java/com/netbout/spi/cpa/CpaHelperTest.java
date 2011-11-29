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

import com.netbout.spi.Bout;
import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import com.netbout.spi.Plain;
import com.netbout.spi.Token;
import com.netbout.spi.plain.PlainBoolean;
import com.netbout.spi.plain.PlainList;
import com.netbout.spi.plain.PlainLong;
import com.netbout.spi.plain.PlainString;
import com.netbout.spi.plain.PlainVoid;
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
        final Identity identity = Mockito.mock(Identity.class);
        this.helper = new CpaHelper(
            identity,
            this.getClass().getPackage().getName()
        );
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
        Mockito.doReturn(new PlainString("alpha-12")).when(token).arg(0);
        Mockito.doReturn(new PlainLong(1L)).when(token).arg(1);
        this.helper.execute(token);
        Mockito.verify(token).result(new PlainBoolean(true));
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
        Mockito.verify(token).result(new PlainVoid());
    }

    /**
     * Test with lists.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testLists() throws Exception {
        final Token token = Mockito.mock(Token.class);
        Mockito.doReturn("list").when(token).mnemo();
        Mockito.doReturn(new PlainLong(4L)).when(token).arg(0);
        this.helper.execute(token);
        Mockito.verify(token).result(Mockito.any(Plain.class));
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
        Mockito.verify(token).result(Mockito.any(PlainList.class));
    }

    /**
     * Helper can't execute unknown operation.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCallToUnknownOperation() throws Exception {
        final Token token = Mockito.mock(Token.class);
        Mockito.doReturn("unknown-operation").when(token).mnemo();
        this.helper.execute(token);
    }

}
