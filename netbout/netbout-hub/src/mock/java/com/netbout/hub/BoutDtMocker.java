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
package com.netbout.hub;

import com.netbout.spi.Urn;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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
        Mockito.doAnswer(
            new Answer<ParticipantDt>() {
                public ParticipantDt answer(final InvocationOnMock invocation) {
                    final Urn name = (Urn) invocation.getArguments()[0];
                    final ParticipantDt dude = new ParticipantDtMocker()
                        .withIdentity(name)
                        .mock();
                    BoutDtMocker.this.withParticipant(dude);
                    return dude;
                }
            }
        ).when(this.bout).addParticipant(Mockito.any(Urn.class));
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
     * With this participant on board.
     * @param participant The participant
     * @return This object
     */
    public BoutDtMocker withParticipant(final Urn participant) {
        this.participants.add(
            new ParticipantDtMocker()
                .withIdentity(participant)
                .mock()
        );
        return this;
    }

    /**
     * Copy it.
     * @return New mocker
     */
    public BoutDtMocker but() {
        final BoutDtMocker copy = new BoutDtMocker(this.bout);
        copy.participants.addAll(this.participants);
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
