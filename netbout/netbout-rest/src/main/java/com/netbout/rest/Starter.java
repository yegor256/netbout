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
package com.netbout.rest;

import com.netbout.bus.Bus;
import com.netbout.bus.DefaultBus;
import com.netbout.hub.DefaultHub;
import com.netbout.hub.Hub;
import com.netbout.spi.Identity;
import com.netbout.spi.cpa.CpaHelper;
import com.ymock.util.Logger;
import java.net.URL;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Starter.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Provider
public final class Starter implements ContextResolver<Starter> {

    /**
     * Public ctor.
     * @param context Servlet context
     * @checkstyle ExecutableStatementCount (3 lines)
     */
    public Starter(@Context final ServletContext context) {
        final long start = System.currentTimeMillis();
        this.start(context);
        Logger.info(
            this,
            "#Starter(%s): done in %dms",
            context.getClass().getName(),
            System.currentTimeMillis() - start
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Starter getContext(final Class<?> cls) {
        throw new UnsupportedOperationException("Starter#getContext()");
    }

    /**
     * Start all.
     * @param context Servlet context
     */
    private void start(@Context final ServletContext context) {
        final Bus bus = new DefaultBus();
        final Hub hub = new DefaultHub(bus);
        context.setAttribute("com.netbout.rest.HUB", hub);
        context.setAttribute("com.netbout.rest.BUS", bus);
        final String uname = "netbout";
        try {
            final Identity idb = hub.user(uname).identity("nb:db");
            hub.promote(idb, new CpaHelper(idb, "com.netbout.db"));
            idb.setPhoto(new URL("http://img.netbout.com/db.png"));
            final Identity ihh = hub.user(uname).identity("nb:hh");
            ihh.setPhoto(new URL("http://img.netbout.com/hh.png"));
            final CpaHelper hhelper = new CpaHelper(ihh, "com.netbout.hub.hh");
            hhelper.contextualize(hub);
            hub.promote(ihh, hhelper);
            final Identity iemail = hub.user(uname).identity("nb:email");
            iemail.setPhoto(new URL("http://img.netbout.com/email.png"));
            hub.promote(
                iemail,
                new CpaHelper(iemail, "com.netbout.notifiers.email")
            );
        } catch (com.netbout.spi.UnreachableIdentityException ex) {
            throw new IllegalStateException(ex);
        } catch (java.net.MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
