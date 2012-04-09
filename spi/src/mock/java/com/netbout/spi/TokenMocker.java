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
package com.netbout.spi;

import java.util.ArrayList;
import java.util.List;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mocker of {@link Token}.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class TokenMocker {

    /**
     * Mocked token.
     */
    private final transient Token token = Mockito.mock(Token.class);

    /**
     * Arguments.
     */
    private final transient List<Plain<?>> args = new ArrayList<Plain<?>>();

    /**
     * Public ctor.
     */
    public TokenMocker() {
        this.withMnemo("some-test-mnemo");
        Mockito.doAnswer(
            new Answer() {
                public Object answer(final InvocationOnMock invocation) {
                    final int pos = (Integer) invocation.getArguments()[0];
                    return TokenMocker.this.args.get(pos);
                }
            }
        ).when(this.token).arg(Mockito.anyInt());
    }

    /**
     * Use this mnemo.
     * @param mnemo The mnemo of operation
     * @return This object
     */
    public TokenMocker withMnemo(final String mnemo) {
        Mockito.doReturn(mnemo).when(this.token).mnemo();
        return this;
    }

    /**
     * Use this argument.
     * @param value The argument
     * @return This object
     */
    public TokenMocker withArg(final Object value) {
        this.args.add(PlainBuilder.fromObject(value));
        return this;
    }

    /**
     * Mock it.
     * @return This object
     */
    public Token mock() {
        return this.token;
    }

}
