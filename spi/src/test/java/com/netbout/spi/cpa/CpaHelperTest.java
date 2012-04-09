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
package com.netbout.spi.cpa;

import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import com.netbout.spi.IdentityMocker;
import com.netbout.spi.Plain;
import com.netbout.spi.Token;
import com.netbout.spi.TokenMocker;
import com.netbout.spi.plain.PlainBoolean;
import com.netbout.spi.plain.PlainList;
import com.netbout.spi.plain.PlainString;
import com.netbout.spi.plain.PlainVoid;
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
        final Identity identity = new IdentityMocker().mock();
        this.helper = new CpaHelper(identity, new FarmMocker().mock());
    }

    /**
     * CpaHelper can discover operations from annotations.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void discoversOperationsInClasses() throws Exception {
        MatcherAssert.assertThat(
            this.helper.supports().size(),
            Matchers.greaterThan(0)
        );
    }

    /**
     * CpaHelper can process tokens with different types of params.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void acceptsTokensWithDifferentTypesOfParams() throws Exception {
        final Token token = new TokenMocker()
            .withMnemo("comparison")
            .withArg("alpha-12")
            .withArg(1L)
            .mock();
        this.helper.execute(token);
        Mockito.verify(token).result(new PlainBoolean(true));
    }

    /**
     * CpaHelper can handle NULL response.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void handlesNullResponse() throws Exception {
        final Token token = new TokenMocker().withMnemo("empty").mock();
        this.helper.execute(token);
        Mockito.verify(token).result(new PlainVoid());
    }

    /**
     * CpaHelper can handle lists are return types.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void handlesListsAsResults() throws Exception {
        final Token token = new TokenMocker().withMnemo("list").mock();
        this.helper.execute(token);
        Mockito.verify(token).result(Mockito.any(Plain.class));
    }

    /**
     * CpaHelper can handle lists of texts.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void handlesTextLists() throws Exception {
        final Token token = new TokenMocker().withMnemo("texts").mock();
        this.helper.execute(token);
        Mockito.verify(token).result(Mockito.any(PlainList.class));
    }

    /**
     * CpaHelper can work properly with UTF-8 texts.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void handlesUnicodeTexts() throws Exception {
        final String text = "\u0443\u0440\u0430!";
        final Token token = new TokenMocker()
            .withMnemo("echo")
            .withArg(text)
            .mock();
        this.helper.execute(token);
        Mockito.verify(token).result(new PlainString(text));
    }

    /**
     * CpaHelper can throw exception if a call is made to an unknown operation.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenAnUnknownOperationCalled() throws Exception {
        this.helper.execute(new TokenMocker().withMnemo("unknown").mock());
    }

}
