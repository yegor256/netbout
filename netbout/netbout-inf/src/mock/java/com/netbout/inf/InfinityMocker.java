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
package com.netbout.inf;

import com.jcabi.log.Logger;
import com.netbout.spi.Urn;
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
     * Wait for eta of provided URNs.
     * @param inf The infinity
     * @param urns The names to wait for
     * @throws InterruptedException If any
     */
    public static void waitFor(final Infinity inf, final Collection<Urn> urns)
        throws InterruptedException {
        final Urn[] names = urns.toArray(new Urn[urns.size()]);
        int cycles = 0;
        while (inf.eta(names) != 0) {
            TimeUnit.SECONDS.sleep(1);
            Logger.debug(InfinityMocker.class, "eta=%[nano]s", inf.eta(names));
            // @checkstyle MagicNumber (1 line)
            if (++cycles > 15) {
                throw new IllegalStateException("time out");
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
