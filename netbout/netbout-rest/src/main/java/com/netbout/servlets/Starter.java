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
package com.netbout.servlets;

import com.netbout.bus.Bus;
import com.netbout.bus.DefaultBus;
import com.netbout.hub.DefaultHub;
import com.netbout.hub.Hub;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Starts entire application.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class Starter implements ServletContextListener {

    /**
     * {@inheritDoc}
     */
    @Override
    public void contextInitialized(final ServletContextEvent event) {
        final Bus bus = new DefaultBus();
        final Hub hub = new DefaultHub(bus);
        event.getServletContext().setAttribute("com.netbout.rest.HUB", hub);
        event.getServletContext().setAttribute("com.netbout.rest.BUS", bus);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void contextDestroyed(final ServletContextEvent event) {
        // ignored
    }

    // /**
    //  * Register basic helper in a hub.
    //  */
    // static {
    //     // @checkstyle MultipleStringLiterals (1 line)
    //     final Identity persistor = HubEntry.user("netbout").identity("nb:db");
    //     persistor.alias("Netbout Database Manager");
    //     // try {
    //     //     persistor.promote(new CpaHelper(persistor, "com.netbout.db"));
    //     // } catch (com.netbout.spi.HelperException ex) {
    //     //     throw new IllegalStateException(ex);
    //     // }
    //     try {
    //         persistor.setPhoto(
    //             new java.net.URL("http://img.netbout.com/db.png")
    //         );
    //     } catch (java.net.MalformedURLException ex) {
    //         throw new IllegalStateException(ex);
    //     }
    // }
    //
    // /**
    //  * Initializer.
    //  */
    // static {
    //     // @checkstyle MultipleStringLiterals (1 line)
    //     final Identity hub = HubEntry.user("netbout").identity("nb:hh");
    //     hub.alias("Netbout Hub");
    //     // try {
    //     //     hub.promote(new CpaHelper(hub, "com.netbout.hub.hh"));
    //     // } catch (com.netbout.spi.HelperException ex) {
    //     //     throw new IllegalStateException(ex);
    //     // }
    //     try {
    //         hub.setPhoto(
    //             new java.net.URL("http://img.netbout.com/hh.png")
    //         );
    //     } catch (java.net.MalformedURLException ex) {
    //         throw new IllegalStateException(ex);
    //     }
    // }

}
