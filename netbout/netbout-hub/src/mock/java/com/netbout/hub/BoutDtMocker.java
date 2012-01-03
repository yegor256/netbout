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

import com.netbout.spi.Urn;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import org.mockito.Mockito;

/**
 * Mocker of {@link BoutDt}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class BoutDtMocker {

    /**
     * The object.
     */
    private final transient BoutDt bout;

    /**
     * Participants.
     */
    private final transient Collection<ParticipantDt> participants =
        new ArrayList<ParticipantDt>();

    /**
     * Messages.
     */
    private final transient List<MessageDt> messages =
        new ArrayList<MessageDt>();

    /**
     * Public ctor.
     */
    public BoutDtMocker() {
        this(Mockito.mock(BoutDt.class));
        this.withNumber(Math.abs(new Random().nextLong()));
    }

    /**
     * Private copy ctor.
     * @param mock The mock to use
     */
    private BoutDtMocker(final BoutDt mock) {
        this.bout = mock;
        Mockito.doReturn(this.participants).when(this.bout).getParticipants();
        Mockito.doReturn(this.messages).when(this.bout).getMessages();
    }

    /**
     * With this number.
     * @param num The number
     * @return This object
     */
    public BoutDtMocker withNumber(final Long num) {
        Mockito.doReturn(num).when(this.bout).getNumber();
        return this;
    }

    /**
     * With this participant on board.
     * @param participant The participant
     * @return This object
     */
    public BoutDtMocker withParticipant(final ParticipantDt participant) {
        this.participants.add(participant);
        return this;
    }

    /**
     * With this message on board.
     * @param msg The message
     * @return This object
     */
    public BoutDtMocker withMessage(final MessageDt msg) {
        this.messages.add(msg);
        return this;
    }

    /**
     * Copy it.
     * @return New mocker
     */
    public BoutDtMocker but() {
        final BoutDtMocker copy = new BoutDtMocker(this.bout);
        copy.participants.addAll(this.participants);
        copy.messages.addAll(this.messages);
        return copy;
    }

    /**
     * Build it.
     * @return The bout
     */
    public BoutDt mock() {
        return this.bout;
    }

}
