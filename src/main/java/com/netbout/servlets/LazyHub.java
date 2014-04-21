/**
 * Copyright (c) 2009-2014, Netbout.com
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
package com.netbout.servlets;

import com.jcabi.log.Logger;
import com.jcabi.log.VerboseThreads;
import com.jcabi.urn.URN;
import com.jcabi.velocity.VelocityPage;
import com.netbout.bus.TxBuilder;
import com.netbout.hub.DefaultHub;
import com.netbout.hub.Hub;
import com.netbout.hub.URNResolver;
import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

/**
 * Lazy HUB.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
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
    private final transient Future<Hub> future;

    /**
     * Public ctor.
     * @param origin Reference to the original hub to use
     */
    private LazyHub(final Future<Hub> origin) {
        this.future = origin;
    }

    /**
     * Build it.
     * @return The hub built
     */
    public static Hub build() {
        final ExecutorService svc =
            Executors.newSingleThreadExecutor(new VerboseThreads());
        final Future<Hub> ftr = svc.submit(
            new Callable<Hub>() {
                @Override
                public Hub call() throws Exception {
                    final long start = System.currentTimeMillis();
                    final Hub hub = new DefaultHub();
                    Logger.info(
                        this,
                        "#call(): HUB built in %[ms]s",
                        System.currentTimeMillis() - start
                    );
                    return hub;
                }
            }
        );
        svc.shutdown();
        return new LazyHub(ftr);
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
    public URNResolver resolver() {
        return this.origin().resolver();
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public Identity identity(final URN name)
        throws Identity.UnreachableURNException {
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
        if (!this.future.isDone()) {
            throw new WebApplicationException(
                Response.status(HttpURLConnection.HTTP_UNAVAILABLE).entity(
                    new VelocityPage("com/netbout/servlets/wait.html.vm").set(
                        "message",
                        Logger.format(
                            "HUB is not ready yet, started to load %[ms]s ago",
                            System.currentTimeMillis() - this.started
                        )
                    ).toString()
                ).header(HttpHeaders.CACHE_CONTROL, "no-cache").build()
            );
        }
        try {
            return this.future.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        } catch (java.util.concurrent.ExecutionException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
