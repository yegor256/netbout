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
package com.netbout.hub.data;

import com.jcabi.log.Logger;
import com.jcabi.urn.URN;
import com.netbout.hub.BoutDt;
import com.netbout.hub.ParticipantDt;
import com.netbout.hub.PowerHub;
import com.netbout.hub.inf.InfBout;
import com.netbout.hub.inf.InfIdentity;
import com.netbout.inf.notices.JoinNotice;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;

/**
 * Bout with data.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class ParticipantData implements ParticipantDt {

    /**
     * Bus to work with.
     */
    private final transient PowerHub hub;

    /**
     * Bout data.
     */
    private final transient BoutDt boutdt;

    /**
     * The participant.
     */
    private final transient URN person;

    /**
     * Is it confirmed?
     */
    private transient Boolean confirmed;

    /**
     * Is he a leader?
     */
    private transient Boolean leader;

    /**
     * Public ctor.
     * @param ihub The hub
     * @param bdata The bout data
     * @param idnt The identity
     */
    public ParticipantData(final PowerHub ihub, final BoutDt bdata,
        final URN idnt) {
        this.hub = ihub;
        assert bdata != null;
        this.boutdt = bdata;
        assert idnt != null;
        this.person = idnt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URN getIdentity() {
        return this.person;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConfirmed(final Boolean flag) {
        synchronized (this.boutdt) {
            this.confirmed = flag;
            this.hub.make("changed-participant-status")
                .asap()
                .arg(this.boutdt.getNumber())
                .arg(this.person)
                .arg(flag)
                .asDefault(true)
                .exec();
            this.hub.infinity().see(
                new JoinNotice() {
                    @Override
                    public Bout bout() {
                        return new InfBout(ParticipantData.this.boutdt);
                    }
                    @Override
                    public Identity identity() {
                        return new InfIdentity(ParticipantData.this.person);
                    }
                }
            );
        }
        Logger.debug(
            this,
            "#setConfirmed(%B): set",
            flag
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean isConfirmed() {
        synchronized (this.boutdt) {
            if (this.confirmed == null) {
                this.confirmed = this.hub.make("get-participant-status")
                    .synchronously()
                    .arg(this.boutdt.getNumber())
                    .arg(this.person)
                    .exec();
            }
        }
        return this.confirmed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLeader(final Boolean flag) {
        synchronized (this.boutdt) {
            this.leader = flag;
            this.hub.make("changed-participant-leadership")
                .asap()
                .arg(this.boutdt.getNumber())
                .arg(this.person)
                .arg(flag)
                .asDefault(true)
                .exec();
        }
        Logger.debug(
            this,
            "#setLeader(%B): set",
            flag
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean isLeader() {
        synchronized (this.boutdt) {
            if (this.leader == null) {
                this.leader = this.hub.make("get-participant-leadership")
                    .synchronously()
                    .arg(this.boutdt.getNumber())
                    .arg(this.person)
                    .exec();
            }
        }
        return this.leader;
    }

}
