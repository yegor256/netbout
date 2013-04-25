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
package com.netbout.servlets;

import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import com.netbout.client.RestSession;
import com.netbout.hub.Hub;
import com.netbout.notifiers.email.EmailFarm;
import java.net.URI;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Application-wide listener that initializes the application on start
 * and shuts it down on stop.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class LifecycleListener implements ServletContextListener {

    /**
     * When was it started.
     */
    private final transient long started = System.currentTimeMillis();

    /**
     * The hub.
     */
    private transient Hub hub;

    /**
     * {@inheritDoc}
     *
     * <p>These attributes is used later in
     * {@link com.netbout.rest.BaseRs#setServletContext(ServletContext)}.
     */
    @Override
    public void contextInitialized(final ServletContextEvent event) {
        final long start = System.nanoTime();
        new RestSession(URI.create("#")).toString();
        try {
            Manifests.append(event.getServletContext());
            this.hub = LazyHub.build();
        } catch (java.io.IOException ex) {
            Logger.error(
                this,
                "#contextInitialized(): %[exception]s",
                ex
            );
            throw new IllegalStateException(ex);
        }
        event.getServletContext().setAttribute(Hub.class.getName(), this.hub);
        EmailFarm.setHub(this.hub);
        Logger.info(
            this,
            "contextInitialized(): done in %[nano]s",
            System.nanoTime() - start
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void contextDestroyed(final ServletContextEvent event) {
        final long start = System.nanoTime();
        if (this.hub == null) {
            Logger.warn(this, "#contextDestroyed(): HUB is null");
        } else {
            try {
                this.hub.close();
            } catch (java.io.IOException ex) {
                Logger.error(
                    this,
                    "#contextDestroyed(): %[exception]s",
                    ex
                );
            }
        }
        Logger.info(
            this,
            "#contextDestroyed(): done in %[nano]s (app was alive for %[ms]s)",
            System.nanoTime() - start,
            System.currentTimeMillis() - this.started
        );
        org.apache.log4j.LogManager.shutdown();
    }

}
