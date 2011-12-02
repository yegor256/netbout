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
import java.util.Collection;
import java.util.ArrayList;
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
    private final transient BoutDt bout = Mockito.mock(BoutDt.class);

    /**
     * Participants.
     */
    private final transient Collection<ParticipantDt> participants =
        new ArrayList<ParticipantDt>();

    /**
     * With this participant on board.
     * @param identity The participant
     * @return This object
     */
    public BoutDtMocker withParticipant(final ParticipantDt participant) {
        this.participants.add(participant);
        return this;
    }

    /**
     * Build it.
     * @return The bout
     */
    public BoutDt mock() {
        Mockito.doReturn(this.participants).when(this.bout).getParticipants();
        Mockito.doReturn(new ParticipantDtMocker().mock()).when(this.bout)
            .addParticipant(Mockito.anyString());
        return this.bout;
    }

}
