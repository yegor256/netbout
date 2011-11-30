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
import com.ymock.util.Logger;

/**
 * Queue of transactions processed by helpers.
 *
 * <p>To execute a transaction you do something like this:
 *
 * <pre>
 * final String[] names = Bus.make("get-user-names")
 *   .inBout(bout)
 *   .arg("Some text argument")
 *   .arg(123L)
 *   .arg(new Date())
 *   .asap()
 *   .expire(".*(user|name).*")
 *   .reportProgress(reporter)
 *   .asPreliminary(null)
 *   .noCache()
 *   .asDefault(new String[] {})
 *   .exec(String[].class)
 * </pre>
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class Bus {

    /**
     * Singleton.
     */
    private static final Bus SINGLETON = new Bus();

    /**
     * Transaction controller.
     */
    private final TxController controller = new DefaultTxController(
        new DefaultTxQueue(), new DefaultTokenCache()
    );

    /**
     * It's a singleton.
     */
    private Bus() {
        Logger.info(
            this,
            "#Bus(): instantiated"
        );
    }

    /**
     * A convenient static method to create a new transaction builder.
     * @param mnemo Mnemo-code of the transation
     * @return The transaction builder
     */
    public static TxBuilder make(final String mnemo) {
        return new DefaultTxBuilder(Bus.SINGLETON.controller, mnemo);
    }

    /**
     * A convenient static method to register new helper.
     * @param helper The helper to register
     */
    public static void register(final Helper helper) {
        Bus.SINGLETON.controller.register(helper);
    }

}
