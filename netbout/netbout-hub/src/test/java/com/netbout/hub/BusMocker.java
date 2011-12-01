/**
 * Copyright (c) 2009-2011, netBout.com
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
package com.netbout.hub;

import com.netbout.bus.Bus;
import com.netbout.bus.TxBuilder;
import org.mockito.Mockito;

/**
 * Mocker of {@link Bus}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class BusMocker {

    /**
     * Mnemo of TxBuilder.
     */
    private transient String mnemo;

    /**
     * Returning value.
     */
    private transient Object value;

    /**
     * Expecting this mnemo.
     * @param name The mnemo name
     */
    public BusMocker expecting(final String name) {
        this.mnemo = name;
        return this;
    }

    /**
     * Returning value.
     * @param val The value
     */
    public BusMocker returning(final Object val) {
        this.value = val;
        return this;
    }

    /**
     * Build it.
     * @return The bus
     */
    public Bus build() {
        final TxBuilder builder = Mockito.mock(TxBuilder.class);
        Mockito.doReturn(builder).when(builder).synchronously();
        Mockito.doReturn(builder).when(builder).asap();
        Mockito.doReturn(builder).when(builder).arg(Mockito.anyObject());
        Mockito.doReturn(builder).when(builder).asDefault(Mockito.anyObject());
        Mockito.doReturn(this.value).when(builder).exec();
        final Bus bus = Mockito.mock(Bus.class);
        Mockito.doReturn(builder).when(bus).make(this.mnemo);
        return bus;
    }

}
