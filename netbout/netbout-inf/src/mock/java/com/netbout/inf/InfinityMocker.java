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
package com.netbout.inf;

import com.jcabi.log.Logger;
import com.jcabi.urn.URN;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.mockito.Mockito;

/**
 * Mocker of {@link Infinity}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class InfinityMocker {

    /**
     * The object.
     */
    private final transient Infinity infinity = Mockito.mock(Infinity.class);

    /**
     * Wait for eta of this infinity.
     * @param inf The infinity
     * @throws InterruptedException If any
     */
    public static void waitFor(final Infinity inf) throws InterruptedException {
        InfinityMocker.waitFor(inf, new ArrayList<URN>());
    }

    /**
     * Wait for eta of provided URNs.
     * @param inf The infinity
     * @param urns The names to wait for
     * @throws InterruptedException If any
     * @checkstyle MagicNumber (20 lines)
     */
    public static void waitFor(final Infinity inf, final Collection<URN> urns)
        throws InterruptedException {
        final URN[] names = urns.toArray(new URN[urns.size()]);
        int cycles = 0;
        while (inf.eta(names) != 0) {
            TimeUnit.MILLISECONDS.sleep(100);
            Logger.debug(InfinityMocker.class, "eta=%[nano]s", inf.eta(names));
            if (++cycles > 500) {
                throw new IllegalStateException(
                    String.format(
                        "time out after %d 100ms cycles of waiting",
                        cycles
                    )
                );
            }
        }
        Logger.debug(
            InfinityMocker.class,
            "INF is ready (eta=%dns, %d deps, maximum=%d)",
            inf.eta(names),
            names.length,
            inf.maximum()
        );
    }

    /**
     * Build it.
     * @return The infinity
     */
    public Infinity mock() {
        return this.infinity;
    }

}
