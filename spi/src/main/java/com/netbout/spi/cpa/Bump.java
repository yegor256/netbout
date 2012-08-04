/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi.cpa;

import com.netbout.spi.Identity;
import java.util.concurrent.TimeUnit;

/**
 * Protector of identity data.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class Bump {

    /**
     * How many nanoseconds of waiting we can afford.
     */
    private static final Long MAX_NANO = 1L * 60 * 1000 * 1000 * 1000;

    /**
     * Parent identity.
     */
    private final transient Identity identity;

    /**
     * Public ctor.
     * @param idnt Identity
     */
    public Bump(final Identity idnt) {
        this.identity = idnt;
    }

    /**
     * Wait for data readiness.
     */
    public void pause() {
        final long start = System.nanoTime();
        int retry = 0;
        while (this.identity.eta() > 0) {
            ++retry;
            if (System.nanoTime() - start > Bump.MAX_NANO) {
                throw new IllegalArgumentException(
                    String.format(
                        "Identity %s still not ready",
                        this.identity.name()
                    )
                );
            }
            try {
                TimeUnit.MILLISECONDS.sleep((long) Math.pow(2, retry));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalArgumentException(ex);
            }
        }
    }

}
