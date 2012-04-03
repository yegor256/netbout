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

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mocker of {@link Bus}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class BusMocker {

    /**
     * The mock.
     */
    private final transient Bus bus = Mockito.mock(Bus.class);

    /**
     * Public ctor.
     */
    public BusMocker() {
        Mockito.doAnswer(
            new Answer() {
                public Object answer(final InvocationOnMock invocation) {
                    return new TxBuilderMocker().mock();
                }
            }
        ).when(this.bus).make(Mockito.anyString());
    }

    /**
     * Expecting this mnemo.
     * @param val The value to return
     * @param mnemo The mnemo name
     * @param args Optional arguments
     * @return This object
     */
    public BusMocker doReturn(final Object val, final String mnemo,
        final Object... args) {
        final TxBuilder builder = new TxBuilderMocker()
            .doReturn(val, args)
            .mock();
        Mockito.doReturn(builder).when(this.bus).make(mnemo);
        return this;
    }

    /**
     * Build it.
     * @return The bus
     */
    public Bus mock() {
        return this.bus;
    }

}
