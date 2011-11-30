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
import com.ymock.util.Logger;

/**
 * Bout with data.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class ParticipantData {

    /**
     * Number of bout.
     */
    private final transient Long bout;

    /**
     * The participant.
     */
    private final transient String identity;

    /**
     * Is it confirmed?
     */
    private transient Boolean confirmed;

    /**
     * Public ctor.
     * @param num The number
     * @param idnt The identity
     */
    private ParticipantData(final Long num, final String idnt) {
        assert num != null;
        this.bout = num;
        assert idnt != null;
        this.identity = idnt;
    }

    /**
     * Build new object.
     * @param num The number
     * @param idnt The identity
     * @return The object
     */
    public static ParticipantData build(final Long num, final String idnt) {
        return new ParticipantData(num, idnt);
    }

    /**
     * Get bout number.
     * @return The identity
     */
    public Long getBout() {
        return this.bout;
    }

    /**
     * Get identity.
     * @return The identity
     */
    public String getIdentity() {
        return this.identity;
    }

    /**
     * Set status.
     * @param flag The flag
     */
    public void setConfirmed(final Boolean flag) {
        this.confirmed = flag;
        Bus.make("changed-participant-status")
            .asap()
            .arg(this.bout)
            .arg(this.identity)
            .arg(this.confirmed)
            .asDefault(true)
            .exec();
        Logger.debug(
            this,
            "#setConfirmed(%b): set",
            this.confirmed
        );
    }

    /**
     * Is it confirmed?
     * @return The flag
     */
    public Boolean isConfirmed() {
        if (this.confirmed == null) {
            this.confirmed = Bus.make("get-participant-status")
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
