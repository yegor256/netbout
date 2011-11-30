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
package com.netbout.bus;

import com.netbout.spi.Helper;
import com.netbout.spi.Plain;

/**
 * Transaction controller, default implementation.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class DefaultTxController implements TxController {

    /**
     * Token executor.
     */
    private final TokenExecutor executor = new DefaultTokenExecutor();

    /**
     * Transaction queue.
     */
    private final TxQueue queue;

    /**
     * Token cache.
     */
    private final TokenCache cache;

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
    public void register(final Helper helper) {
        this.executor.register(helper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Plain<?> exec(final Transaction trans) {
        final TxToken token = trans.makeToken();
        if (trans.isInsideBout()) {
            this.executor.exec(token, trans.getBout());
        } else {
            this.executor.exec(token);
        }
        Plain<?> result;
        if (token.isCompleted()) {
            result = token.getResult();
        } else {
            result = trans.getDefaultResult();
        }
        return result;
    }

}
