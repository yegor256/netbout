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
package com.netbout.hub.data;

import com.netbout.bus.Bus;
import com.netbout.hub.ParticipantDt;
import com.netbout.spi.Urn;
import com.ymock.util.Logger;

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
    private final transient Bus bus;

    /**
     * Number of bout.
     */
    private final transient Long bout;

    /**
     * The participant.
     */
    private final transient Urn identity;

    /**
     * Is it confirmed?
     */
    private transient Boolean confirmed;

    /**
     * Public ctor.
     * @param ibus The bus
     * @param num The number
     * @param idnt The identity
     */
    public ParticipantData(final Bus ibus, final Long num, final Urn idnt) {
        this.bus = ibus;
        assert num != null;
        this.bout = num;
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
        this.confirmed = true;
        this.bus.make("changed-participant-status")
            .asap()
            .arg(this.bout)
            .arg(this.identity)
            .arg(flag)
            .asDefault(true)
            .exec();
        Logger.debug(
            this,
            "#setConfirmed(): set"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean isConfirmed() {
        if (this.confirmed == null) {
            this.confirmed = this.bus.make("get-participant-status")
                .synchronously()
                .arg(this.bout)
                .arg(this.identity)
                .exec();
            Logger.debug(
                this,
                "#isConfirmed(): status loaded as %b for dude '%s' in bout #%d",
                this.confirmed,
                this.identity,
                this.bout
            );
        }
        return this.confirmed;
    }

}
