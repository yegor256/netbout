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

import com.jcabi.log.Logger;
import com.jcabi.velocity.VelocityPage;
import com.netbout.bus.TxBuilder;
import com.netbout.hub.Hub;
import com.netbout.hub.UrnResolver;
import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import com.netbout.spi.UnreachableUrnException;
import com.netbout.spi.Urn;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Lazy HUB.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class LazyHub implements Hub {

    /**
     * When was it started.
     */
    private final transient long started = System.currentTimeMillis();

    /**
     * Reference to the real hub.
     */
    private final transient AtomicReference<Hub> ref;

    /**
     * Public ctor.
     * @param origin Reference to the original hub to use
     */
    public LazyHub(final AtomicReference<Hub> origin) {
        this.ref = origin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.origin().close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UrnResolver resolver() {
        return this.origin().resolver();
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (3 lines)
     */
    @Override
    public Identity identity(final Urn name) throws UnreachableUrnException {
        return this.origin().identity(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TxBuilder make(final String mnemo) {
        return this.origin().make(mnemo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Helper promote(final Identity identity, final URL location) {
        return this.origin().promote(identity, location);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity join(final Identity main, final Identity child) {
        return this.origin().join(main, child);
    }

    /**
     * Get original hub.
     * @return The hub
     */
    private Hub origin() {
        if (this.ref.get() == null) {
            throw new WebApplicationException(
                Response.ok().entity(
                    new VelocityPage("com/netbout/servlets/wait.html.vm").set(
                        "message",
                        Logger.format(
                            "HUB is not ready yet, started to load %[ms]s ago",
                            System.currentTimeMillis() - this.started
                        )
                    ).toString()
                ).build()
            );
        }
        return this.ref.get();
    }

}
