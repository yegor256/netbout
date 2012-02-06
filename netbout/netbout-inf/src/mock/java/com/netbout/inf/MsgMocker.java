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
package com.netbout.inf;

import org.mockito.Mockito;

/**
 * Mocker of {@link Msg}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class MsgMocker {

    /**
     * The object.
     */
    private final transient Msg msg = Mockito.mock(Msg.class);

    /**
     * Public ctor.
     */
    public MsgMocker() {
        Mockito.doReturn("").when(this.msg).get(Mockito.anyString());
    }

    /**
     * With this bout number.
     * @param num The number
     * @return This object
     */
    public MsgMocker withBoutNumber(final Long num) {
        Mockito.doReturn(num).when(this.msg).bout();
        return this;
    }

    /**
     * With this property.
     * @param name Name of prop
     * @param value Name of prop
     * @return This object
     */
    public MsgMocker with(final String name, final Object value) {
        Mockito.doReturn(value).when(this.msg).get(name);
        Mockito.doReturn(true).when(this.msg).has(name, value);
        return this;
    }

    /**
     * Build it.
     * @return The msg
     */
    public Msg mock() {
        return this.msg;
    }

}
