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

import com.netbout.hub.queue.HelpQueue;
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
    private final Long bout;

    /**
     * The participant.
     */
    private final String identity;

    /**
     * Is it confirmed?
     */
    private Boolean confirmed;

    /**
     * Public ctor.
     * @param num The number
     * @param idnt The identity
     */
    public ParticipantData(final Long num, final String idnt) {
        this.bout = num;
        this.identity = idnt;
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
        HelpQueue.make("changed-participant-confirm-status")
            .priority(HelpQueue.Priority.ASAP)
            .arg(this.getBout().toString())
            .arg(this.getIdentity())
            .arg(this.confirmed.toString())
            .exec(Boolean.class);
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
            this.confirmed = HelpQueue.make("get-participant-confirm-status")
                .priority(HelpQueue.Priority.SYNCHRONOUSLY)
                .arg(this.getBout().toString())
                .arg(this.getIdentity())
                .exec(Boolean.class);
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
