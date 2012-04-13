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
package com.netbout.hub.inf;

import com.netbout.hub.BoutDt;
import com.netbout.hub.MessageDt;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import java.util.Date;

/**
 * Message we push to INF.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class InfMessage implements Message {

    /**
     * Message data.
     */
    private final transient MessageDt data;

    /**
     * Public ctor.
     * @param msg Message data
     */
    public HubMessage(final MessageDt msg) {
        this.data = msg;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Message msg) {
        throw new UnsupportedOperationException("#compareTo()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object bout) {
        throw new UnsupportedOperationException("#equals()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("#hashCode()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        throw new UnsupportedOperationException("#toString()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout bout() {
        throw new UnsupportedOperationException("#bout()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long number() {
        return this.data.getNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity author() {
        return new InfIdentity(this.data.getAuthor());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String text() {
        return this.data.getText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date date() {
        return this.data.getDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean seen() {
        throw new UnsupportedOperationException("#seen()");
    }

}
