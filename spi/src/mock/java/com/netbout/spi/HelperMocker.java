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

import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mocker of {@link Helper}.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class HelperMocker {

    /**
     * Mocked helper.
     */
    private final Helper helper = Mockito.mock(Helper.class);

    /**
     * Map of requests and responses.
     */
    private final ConcurrentMap<String, Object> ops =
        new ConcurrentHashMap<String, Object>();

    /**
     * Public ctor.
     */
    public HelperMocker() {
        Mockito.doReturn(this.ops.keySet()).when(this.helper).supports();
        Mockito.doAnswer(
            new Answer() {
                @Override
                public Object answer(final InvocationOnMock invocation) {
                    final Token token = (Token) invocation.getArguments()[0];
                    final String mnemo = token.mnemo();
                    if (HelperMocker.this.ops.containsKey(mnemo)) {
                        token.result(
                            PlainBuilder.fromObject(
                                HelperMocker.this.ops.get(mnemo)
                            )
                        );
                    }
                    return true;
                }
            }
        ).when(this.helper).execute(Mockito.any(Token.class));
        this.withLocation("http://localhost/URL-set-by-HelperMocker");
    }

    /**
     * Return this value on this request.
     * @param value The value to return
     * @param mnemo The mnemo of operation
     * @return This object
     */
    public HelperMocker doReturn(final Object value, final String mnemo) {
        this.ops.put(mnemo, value);
        return this;
    }

    /**
     * With this location.
     * @param url The location
     * @return This object
     */
    public HelperMocker withLocation(final String url) {
        try {
            Mockito.doReturn(new URL(url)).when(this.helper).location();
        } catch (java.net.MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
        return this;
    }

    /**
     * Mock it.
     * @return This object
     */
    public Helper mock() {
        return this.helper;
    }

}
