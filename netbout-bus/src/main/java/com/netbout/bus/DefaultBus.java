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
package com.netbout.bus;

import com.jcabi.log.Logger;
import com.netbout.bh.StageFarm;
import com.netbout.bh.StatsProvider;
import com.netbout.bus.cache.EmptyTokenCache;
import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import javax.validation.constraints.NotNull;

/**
 * Default implementation of {@link Bus}.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.DoNotUseThreads")
public final class DefaultBus implements Bus, StatsProvider {

    /**
     * Transaction controller.
     */
    private final transient TxController controller = new DefaultTxController(
        new DefaultTxQueue(),
        new EmptyTokenCache()
    );

    /**
     * Public ctor.
     */
    public DefaultBus() {
        StageFarm.register(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        this.make("shutdown")
            .synchronously()
            .asDefault(false)
            .exec();
        Logger.info(this, "#close(): BUS closed");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TxBuilder make(@NotNull final String mnemo) {
        return new DefaultTxBuilder(this.controller, mnemo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(@NotNull final Identity identity,
        @NotNull final Helper helper) {
        this.controller.register(identity, helper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String statistics() {
        return ((StatsProvider) this.controller).statistics();
    }

}
