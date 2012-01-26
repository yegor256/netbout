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

import com.netbout.bus.DefaultBus;
import com.netbout.hub.DefaultHub;
import com.netbout.hub.Hub;
import com.netbout.spi.Identity;
import com.netbout.spi.Urn;
import com.netbout.utils.Promoter;
import com.ymock.util.Logger;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
        final Hub hub = new DefaultHub(new DefaultBus());
        this.start(hub);
        context.setAttribute("com.netbout.rest.HUB", hub);
        Logger.info(
            this,
            "#Starter(%[type]s): done in %dms",
            context,
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
     * @param hub The hub to work with
     */
    private void start(final Hub hub) {
        final Promoter promoter = new Promoter(hub);
        final Identity persister = this.persister(hub, promoter);
        final List<Urn> helpers = hub.make("get-all-helpers")
            .synchronously()
            .asDefault(new ArrayList<Urn>())
            .exec();
        for (Urn name : helpers) {
            if (name.equals(persister.name())) {
                continue;
            }
            final URL url = hub.make("get-helper-url")
                .synchronously()
                .arg(name)
                .exec();
            try {
                promoter.promote(persister.friend(name), url);
            } catch (com.netbout.spi.UnreachableUrnException ex) {
                Logger.error(
                    this,
                    "#start(): failed to create '%s' identity:\n%[exception]s",
                    name,
                    ex
                );
            }
        }
    }

    /**
     * Create persister's identity.
     * @param hub The hub
     * @param promoter The promoter
     * @return The persister
     */
    private Identity persister(final Hub hub, final Promoter promoter) {
        final Identity persister;
        try {
            persister = hub.identity(new Urn("netbout", "db"));
        } catch (com.netbout.spi.UnreachableUrnException ex) {
            throw new IllegalStateException(
                "Failed to create starter's identity",
                ex
            );
        }
        try {
            promoter.promote(
                persister,
                new URL("file", "", "com.netbout.db")
            );
        } catch (java.net.MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
        return persister;
    }

}
