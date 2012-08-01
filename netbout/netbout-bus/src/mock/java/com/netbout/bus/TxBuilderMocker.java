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
package com.netbout.bus;

import com.netbout.spi.Bout;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mocker of {@link TxBuilder}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class TxBuilderMocker {

    /**
     * The mock.
     */
    private final transient TxBuilder builder = Mockito.mock(TxBuilder.class);

    /**
     * Value to return.
     */
    private transient Object value;

    /**
     * Public ctor.
     */
    public TxBuilderMocker() {
        Mockito.doReturn(this.builder).when(this.builder).synchronously();
        Mockito.doReturn(this.builder).when(this.builder).asap();
        Mockito.doReturn(this.builder).when(this.builder)
            .expire(Mockito.anyString());
        Mockito.doReturn(this.builder).when(this.builder)
            .arg(Mockito.anyObject());
        Mockito.doReturn(this.builder).when(this.builder)
            .inBout(Mockito.any(Bout.class));
        Mockito.doReturn(this.builder).when(this.builder).noCache();
        Mockito.doAnswer(
            new Answer<TxBuilder>() {
                public TxBuilder answer(final InvocationOnMock invocation) {
                    if (TxBuilderMocker.this.value == null) {
                        TxBuilderMocker.this.value =
                            invocation.getArguments()[0];
                    }
                    return TxBuilderMocker.this.builder;
                }
            }
        ).when(this.builder).asDefault(Mockito.anyObject());
        Mockito.doAnswer(
            new Answer<Object>() {
                public Object answer(final InvocationOnMock invocation) {
                    if (TxBuilderMocker.this.value == null) {
                        throw new IllegalStateException(
                            "Default not set and doReturn() not specified"
                        );
                    }
                    return TxBuilderMocker.this.value;
                }
            }
        ).when(this.builder).exec();
    }

    /**
     * Expecting this mnemo.
     * @param val The value to return
     * @param args Optional arguments
     * @return This object
     */
    public TxBuilderMocker doReturn(final Object val, final Object... args) {
        this.value = val;
        return this;
    }

    /**
     * Build it.
     * @return The this
     */
    public TxBuilder mock() {
        return this.builder;
    }

}
