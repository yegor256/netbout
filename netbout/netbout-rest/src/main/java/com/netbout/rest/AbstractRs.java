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
import com.ymock.util.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
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
     * Injected by JAX-RS, because of <tt>&#64;Context</tt> annotation.
     */
    @Context
    private HttpHeaders httpHeaders;

    /**
     * Injected by JAX-RS, because of <tt>&#64;Context</tt> annotation.
     */
    @Context
    private HttpServletRequest httpServletRequest;

    /**
     * Injected by JAX-RS, because of <tt>&#64;Context</tt> annotation.
     */
    @CookieParam("netbout")
    private String cookie;

    /**
     * {@inheritDoc}
     */
    @Override
    public Entry entry() {
        return this.entry;
    }

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
    public HttpHeaders httpHeaders() {
        if (this.httpHeaders == null) {
            throw new IllegalStateException(
                String.format(
                    "%s#httpHeaders was never injected by JAX-RS",
                    this.getClass().getName()
                )
            );
        }
        return this.httpHeaders;
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
     * {@inheritDoc}
     */
    @Override
    public HttpServletRequest httpServletRequest() {
        if (this.httpServletRequest == null) {
            throw new IllegalStateException(
                String.format(
                    "%s#httpServletRequest was never injected by JAX-RS",
                    this.getClass().getName()
                )
            );
        }
        return this.httpServletRequest;
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

    /**
     * Set HttpHeaders, to be called by JAX-RS framework or a unit test.
     * @param hdrs List of headers
     */
    public void setHttpHeaders(final HttpHeaders hdrs) {
        this.httpHeaders = hdrs;
    }

    /**
     * Set HttpServletRequest, to be called by JAX-RS framework or a unit test.
     * @param request The request
     */
    public void setHttpServletRequest(final HttpServletRequest request) {
        this.httpServletRequest = request;
    }

    /**
     * Get current user identity, or throws {@link LoginRequiredException} if
     * no user is logged in at the moment.
     * @return The identity
     */
    protected final Identity identity() {
        if (this.entry == null) {
            throw new IllegalStateException(
                String.format(
                    "%s#entry was never injected by JAX-RS",
                    this.getClass().getName()
                )
            );
        }
        if (this.cookie == null) {
            throw new LoginRequiredException();
        }
        try {
            return new Cryptor(this.entry()).decrypt(this.cookie);
        } catch (Cryptor.DecryptionException ex) {
            Logger.warn(
                this,
                "Decryption failure from %s calling '%s': %s",
                this.httpServletRequest().getRemoteAddr(),
                this.httpServletRequest().getRequestURI(),
                ex.getMessage()
            );
            throw new LoginRequiredException();
        }
    }

}
