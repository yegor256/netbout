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

import com.netbout.hub.HubEntry;
import com.netbout.spi.Entry;
import com.netbout.spi.Identity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

/**
 * Abstract RESTful resource.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
abstract class AbstractRs implements Resource {

    /**
     * Entry to work with.
     */
    private Entry entry = new HubEntry();

    /**
     * Injected by JAX-RS, because of <tt>&#64;Context</tt> annotation.
     */
    @Context
    private Providers providers;

    /**
     * Injected by JAX-RS, because of <tt>&#64;Context</tt> annotation.
     */
    @Context
    private UriInfo uriInfo;

    /**
     * {@inheritDoc}
     */
    @Override
    public Providers providers() {
        if (this.providers == null) {
            throw new IllegalStateException(
                String.format(
                    "%s#providers was never injected by JAX-RS",
                    this.getClass().getName()
                )
            );
        }
        return this.providers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Identity identity() {
        if (this.entry == null) {
            throw new IllegalStateException(
                String.format(
                    "%s#entry was never injected by JAX-RS",
                    this.getClass().getName()
                )
            );
        }
        throw new LoginRequiredException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UriInfo uriInfo() {
        if (this.uriInfo == null) {
            throw new IllegalStateException(
                String.format(
                    "%s#uriInfo was never injected by JAX-RS",
                    this.getClass().getName()
                )
            );
        }
        return this.uriInfo;
    }

    /**
     * Set new entry.
     * @param ent The entry to work with
     */
    public final void setEntry(final Entry ent) {
        this.entry = ent;
    }

    /**
     * Set URI Info, to be called by unit test.
     * @param info The info to inject
     */
    public void setUriInfo(final UriInfo info) {
        this.uriInfo = info;
    }

    /**
     * Set Providers, to be called by JAX-RS framework or a unit test.
     * @param prov List of providers
     */
    public void setProviders(final Providers prov) {
        this.providers = prov;
    }

}
