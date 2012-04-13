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
package com.netbout.bus;

import com.netbout.bh.StatsProvider;
import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import com.netbout.spi.Plain;

/**
 * Transaction controller, default implementation.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class DefaultTxController implements TxController, StatsProvider {

    /**
     * Token executor.
     */
    private final transient TokenExecutor executor = new DefaultTokenExecutor();

    /**
     * Transaction queue.
     */
    @SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
    private final transient TxQueue queue;

    /**
     * Token cache.
     */
    private final transient TokenCache cache;

    /**
     * Public ctor.
     * @param que The queue
     * @param cac The cache
     */
    public DefaultTxController(final TxQueue que, final TokenCache cac) {
        this.queue = que;
        this.cache = cac;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(final Identity identity, final Helper helper) {
        this.executor.register(identity, helper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Plain<?> exec(final Transaction trans) {
        final TxToken token = trans.makeToken();
        this.cache.resolve(token);
        Plain<?> result;
        if (token.isCompleted()) {
            result = token.getResult();
        } else {
            result = this.retrieve(trans, token);
            if (trans.isCacheEnabled()) {
                this.cache.save(token, result);
            }
        }
        this.clearCacheAfter(trans);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String statistics() {
        return ((StatsProvider) this.executor).statistics();
    }

    /**
     * Retrieves result of the token through executor.
     * @param trans The transaction
     * @param token The token
     * @return The result
     */
    private Plain<?> retrieve(final Transaction trans, final TxToken token) {
        Plain<?> result;
        if (trans.isInsideBout()) {
            this.executor.exec(token, trans.getBout());
        } else {
            this.executor.exec(token);
        }
        if (token.isCompleted()) {
            result = token.getResult();
        } else {
            result = trans.getDefaultValue();
        }
        return result;
    }

    /**
     * Clear cache after execution, if necessary.
     * @param trans The transaction
     */
    private void clearCacheAfter(final Transaction trans) {
        if (trans.hasToExpireOthers()) {
            this.cache.delete(trans.getExpirePattern());
        }
    }

}
