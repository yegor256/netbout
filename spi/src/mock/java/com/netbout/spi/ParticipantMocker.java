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

import org.mockito.Mockito;

/**
 * Mocker of {@link Participant}.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class ParticipantMocker {

    /**
     * Mocked participant.
     */
    private final Participant participant = Mockito.mock(Participant.class);

    /**
     * Public ctor.
     */
    public ParticipantMocker() {
        this.withIdentity(new IdentityMocker().mock());
        this.inBout(new BoutMocker().mock());
    }

    /**
     * This is the participant.
     * @param The identity
     * @return This object
     */
    public ParticipantMocker withIdentity(final Identity identity) {
        Mockito.doReturn(identity).when(this.participant).identity();
        return this;
    }

    /**
     * In bout.
     * @param The bout
     * @return This object
     */
    public ParticipantMocker inBout(final Bout bout) {
        Mockito.doReturn(bout).when(this.participant).bout();
        return this;
    }

    /**
     * Mock it.
     * @return Mocked participant
     */
    public Participant mock() {
        return this.participant;
    }

}
