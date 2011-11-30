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

import com.netbout.spi.Bout;
import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import com.netbout.spi.Participant;
import com.ymock.util.Logger;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Default executor of a token.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class DefaultTokenExecutor implements TokenExecutor {

    /**
     * List of registered helpers.
     */
    private final Set<Helper> helpers = new CopyOnWriteArraySet<Helper>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(final Helper helper) {
        this.helpers.add(helper);
        Logger.debug(
            this,
            "#register(%s): registered (%d total now)",
            helper,
            this.helpers.size()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exec(final TxToken token) {
        this.run(token, this.helpers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exec(final TxToken token, final Bout bout) {
        final Set<Helper> active = new HashSet<Helper>();
        for (Participant participant : bout.participants()) {
            final Identity identity = participant.identity();
            if (this.helpers.contains(identity)) {
                active.add((Helper) identity);
            }
        }
        this.run(token, active);
    }

    /**
     * Execute given token with a given set of helpers.
     * @param token The token to execute
     * @param targets The helpers to use
     */
    private void run(final TxToken token, final Set<Helper> targets) {
        final long start = System.currentTimeMillis();
        for (Helper helper : targets) {
            if (helper.supports().contains(token.mnemo())) {
                helper.execute(token);
                if (token.isCompleted()) {
                    break;
                }
            }
        }
        Logger.debug(
            this,
            "#run(%s, %d helpers): executed in %dms",
            token,
            targets.size(),
            System.currentTimeMillis() - start
        );
    }

}
