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
package com.netbout.servlets;

import com.jcabi.log.VerboseRunnable;
import com.netbout.hub.DefaultHub;
import com.netbout.hub.Hub;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Background builder of a HUB.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.DoNotUseThreads")
final class HubBuilder {

    /**
     * Reference to the hub.
     */
    private transient AtomicReference<Hub> ref;

    /**
     * Build it and return.
     * @return The hub
     */
    public Hub build() {
        final Thread loader = new Thread(
            new VerboseRunnable(
                new Runnable() {
                    @Override
                    public void run() {
                        final long started = System.currentTimeMillis();
                        try {
                            HubBuilder.this.ref.set(new DefaultHub());
                        } catch (java.io.IOException ex) {
                            throw new IllegalStateException(ex);
                        }
                        Logger.info(this, "#run(): HUB built in %[ms]s");
                    }
                }
            )
        );
        loader.start();
        return new LazyHub(this.ref);
    }

}
