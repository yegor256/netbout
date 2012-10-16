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
 * this code accidentally and without intent to use it, please report this
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
import com.netbout.hub.ParticipantDt;
import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import com.netbout.spi.Message;
import com.netbout.spi.Participant;
import com.netbout.spi.Query;
import java.util.Collection;
import java.util.Date;

/**
 * Bout we push to identity.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class InfBout implements Bout {

    /**
     * The data.
     */
    private final transient BoutDt data;

    /**
     * Public ctor.
     * @param bout The data
     */
    public InfBout(final BoutDt bout) {
        this.data = bout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Bout bout) {
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
    public Long number() {
        return this.data.getNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String title() {
        return this.data.getTitle();
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
    public void confirm() {
        throw new UnsupportedOperationException("#confirm()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void leave() {
        throw new UnsupportedOperationException("#leave()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rename(final String text) {
        throw new UnsupportedOperationException("#rename()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Participant invite(final Friend friend) {
        throw new UnsupportedOperationException("#invite()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Participant> participants() {
        return new ParticipantDt.Participants(this.data.getParticipants());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Message> messages(final Query query) {
        throw new UnsupportedOperationException("#messages()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message message(final Long num) {
        throw new UnsupportedOperationException("#message()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message post(final String text) {
        throw new UnsupportedOperationException("#post()");
    }

}
