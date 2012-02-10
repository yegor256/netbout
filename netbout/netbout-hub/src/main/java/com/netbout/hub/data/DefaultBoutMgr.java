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

import com.netbout.hub.BoutMgr;
import com.netbout.hub.Hub;
import com.netbout.spi.BoutNotFoundException;
import com.netbout.spi.Urn;
import com.ymock.util.Logger;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manager of all bouts.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class DefaultBoutMgr implements BoutMgr {

    /**
     * Recently seen bouts.
     */
    private final transient ConcurrentMap<Long, BoutData> bouts =
        new ConcurrentHashMap<Long, BoutData>();

    /**
     * Bus to work with.
     */
    private final transient Hub hub;

    /**
     * Public ctor, for JAXB.
     */
    public DefaultBoutMgr() {
        throw new IllegalStateException("illegal call");
    }

    /**
     * Public ctor.
     * @param ihub The hub
     */
    public DefaultBoutMgr(final Hub ihub) {
        this.hub = ihub;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String statistics() {
        final StringBuilder text = new StringBuilder();
        // todo
        return text.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long create(final Urn author) {
        final Long number = this.hub
            // @checkstyle MultipleStringLiterals (1 lines)
            .make("get-next-bout-number")
            .synchronously()
            .asDefault(this.defaultNextBoutNumber())
            .exec();
        this.hub.make("started-new-bout")
            .asap()
            .arg(number)
            // @checkstyle MultipleStringLiterals (1 lines)
            .expire("get-next-bout-number")
            .asDefault(true)
            .exec();
        BoutData data;
        try {
            data = this.find(number);
        } catch (com.netbout.spi.BoutNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
        data.setTitle("");
        data.addParticipant(author).setConfirmed(true);
        Logger.debug(
            this,
            "#create(): bout #%d created",
            data.getNumber()
        );
        return data.getNumber();
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (3 lines)
     */
    @Override
    public BoutData find(final Long number) throws BoutNotFoundException {
        synchronized (this) {
            assert number != null;
            if (!this.bouts.containsKey(number)) {
                final Boolean exists = this.hub
                    .make("check-bout-existence")
                    .synchronously()
                    .arg(number)
                    .asDefault(true)
                    .exec();
                if (!exists) {
                    throw new BoutNotFoundException(number);
                }
                this.bouts.put(number, new BoutData(this.hub, number));
                Logger.debug(
                    this,
                    "#find(#%d): bout data restored",
                    number
                );
            }
            return this.bouts.get(number);
        }
    }

    /**
     * Get next bout number, which is by default.
     * @return The number
     */
    private Long defaultNextBoutNumber() {
        Long number;
        if (this.bouts.isEmpty()) {
            number = 1L;
        } else {
            number = Collections.max(this.bouts.keySet()) + 1;
        }
        return number;
    }

}
