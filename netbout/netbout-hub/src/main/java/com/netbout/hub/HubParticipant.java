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

import com.jcabi.log.Logger;
import com.netbout.spi.Friend;
import com.netbout.spi.Participant;
import com.netbout.spi.Urn;

/**
 * Identity.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class HubParticipant implements Participant {

    /**
     * The data.
     */
    private final transient ParticipantDt data;

    /**
     * Data of bout.
     */
    private final transient BoutDt boutdt;

    /**
     * Public ctor.
     * @param dat The data
     * @param bdt Bout data
     */
    public HubParticipant(final ParticipantDt dat, final BoutDt bdt) {
        this.data = dat;
        this.boutdt = bdt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Friend friend) {
        return this.name().compareTo(friend.name());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.data.getIdentity().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Urn name() {
        return this.data.getIdentity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void kickOff() {
        this.boutdt.kickOff(this.name());
        Logger.info(
            this,
            "Participant '%s' was kicked-off from bout #%d",
            this.name(),
            this.boutdt.getNumber()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean confirmed() {
        return this.data.isConfirmed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean leader() {
        return this.data.isLeader();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void consign() {
        this.boutdt.setLeader(this.name());
    }

}
