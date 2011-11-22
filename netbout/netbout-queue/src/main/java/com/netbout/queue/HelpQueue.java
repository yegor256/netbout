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
package com.netbout.queue;

import com.netbout.spi.Helper;
import com.ymock.util.Logger;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Queue of transactions processed by helpers.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class HelpQueue {

    /**
     * Priority.
     */
    public enum Priority {
        /**
         * Run it immediately.
         */
        SYNCHRONOUSLY,
        /**
         * Run it as soon as possible.
         */
        ASAP,
        /**
         * Just normal execution.
         */
        NORMAL
    }

    /**
     * List of registered helpers.
     */
    private static final List<Helper> HELPERS =
        new CopyOnWriteArrayList<Helper>();

    /**
     * It's a utility class.
     */
    private HelpQueue() {
        // empty
    }

    /**
     * Register new helper.
     * @param helper The helper to register
     */
    public static void register(final Helper helper) {
        HelpQueue.HELPERS.add(helper);
    }

    /**
     * Create one transaction.
     * @param mnemo Mnemo-code of the request
     * @return The transaction
     */
    public static Transaction make(final String mnemo) {
        return new Transaction(mnemo);
    }

    /**
     * Execute one transaction.
     * @param trans The transaction to execute
     * @return The result
     */
    protected static String execute(final Transaction trans) {
        final String mnemo = trans.getMnemo();
        String result = null;
        for (Helper helper : HelpQueue.HELPERS) {
            try {
                if (helper.supports().contains(mnemo)) {
                    result = helper.execute(mnemo, trans.getArgs());
                    if (result != null) {
                        break;
                    }
                }
            } catch (com.netbout.spi.HelperException ex) {
                throw new IllegalArgumentException(
                    String.format(
                        "Failed to execute '%s'",
                        mnemo
                    ),
                    ex
                );
            }
        }
        if (result == null) {
            result = trans.getDefault();
            Logger.debug(
                HelpQueue.class,
                "#execute(%s): no helpers found, returning default: '%s'",
                trans.getMnemo(),
                result
            );
        }
        return result;
    }

}
