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
package com.netbout.hub.data;

import com.netbout.hub.Hub;
import com.netbout.hub.ParticipantDt;
import com.netbout.spi.Urn;
import com.jcabi.log.Logger;

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
    private final transient Hub hub;

    /**
     * Bout data.
     */
    private final transient BoutDt boutdt;

    /**
     * The participant.
     */
    private final transient Urn identity;

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
     * @param num The number
     * @param idnt The identity
     */
    public ParticipantData(final Hub ihub, final BoutDt bdata, final Urn idnt) {
        this.hub = ihub;
        assert bdata != null;
        this.boutdt = bdata;
        assert idnt != null;
        this.identity = idnt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getBout() {
        return this.bout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Urn getIdentity() {
        return this.identity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConfirmed(final Boolean flag) {
        synchronized (this.bout) {
            this.confirmed = flag;
            this.hub.make("changed-participant-status")
                .asap()
                .arg(this.bout)
                .arg(this.identity)
                .arg(flag)
                .asDefault(true)
                .exec();
            this.hub.see(
                new ParticipationConfirmedNotice() {
                    @Override
                    public Bout bout() {
                        return new InfMessage(ParticipantData.this.boutdt);
                    }
                    @Override
                    public Identity confirmedBy() {
                        return new InfIdentity(ParticipantData.this.identity);
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
        synchronized (this.bout) {
            if (this.confirmed == null) {
                this.confirmed = this.hub.make("get-participant-status")
                    .synchronously()
                    .arg(this.bout)
                    .arg(this.identity)
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
        synchronized (this.bout) {
            this.leader = flag;
            this.hub.make("changed-participant-leadership")
                .asap()
                .arg(this.bout)
                .arg(this.identity)
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
        synchronized (this.bout) {
            if (this.leader == null) {
                this.leader = this.hub.make("get-participant-leadership")
                    .synchronously()
                    .arg(this.bout)
                    .arg(this.identity)
                    .exec();
            }
        }
        return this.leader;
    }

}
