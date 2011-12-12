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
import com.netbout.hub.Hub;
import com.netbout.spi.Identity;
import com.netbout.utils.Cryptor;
import com.ymock.util.Logger;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import org.apache.commons.codec.binary.Base64;

/**
 * Abstract RESTful resource.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public abstract class AbstractRs implements Resource {

    /**
     * Name of the authentication parameter.
     */
    public static final String AUTH_PARAM = "auth";

    /**
     * When this resource was started, in nanoseconds.
     */
    private final transient long inano = System.nanoTime();

    /**
     * Hub to work with.
     */
    private transient Hub ihub;

    /**
     * Bus to work with.
     */
    private transient Bus ibus;

    /**
     * List of known JAX-RS providers.
     */
    private transient Providers iproviders;

    /**
     * URI info.
     */
    private transient UriInfo iuriInfo;

    /**
     * Http headers.
     */
    private transient HttpHeaders ihttpHeaders;

    /**
     * HTTP servlet request.
     */
    private transient HttpServletRequest ihttpRequest;

    /**
     * Cookie.
     */
    private transient String icookie;

    /**
     * Shall we add AUTH to URLs?
     */
    private transient boolean addAuthToURIs;

    /**
     * The message to show.
     */
    private transient String imessage = "";

    /**
     * {@inheritDoc}
     */
    @Override
    public final long nano() {
        return this.inano;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Providers providers() {
        if (this.iproviders == null) {
            throw new IllegalStateException(
                String.format(
                    "%s#providers was never injected by JAX-RS",
                    this.getClass().getName()
                )
            );
        }
        return this.iproviders;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final HttpHeaders httpHeaders() {
        if (this.ihttpHeaders == null) {
            throw new IllegalStateException(
                String.format(
                    "%s#httpHeaders was never injected by JAX-RS",
                    this.getClass().getName()
                )
            );
        }
        return this.ihttpHeaders;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final UriInfo uriInfo() {
        if (this.iuriInfo == null) {
            throw new IllegalStateException(
                String.format(
                    "%s#uriInfo was never injected by JAX-RS",
                    this.getClass().getName()
                )
            );
        }
        return this.iuriInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final HttpServletRequest httpServletRequest() {
        if (this.ihttpRequest == null) {
            throw new IllegalStateException(
                String.format(
                    "%s#httpRequest was never injected by JAX-RS",
                    this.getClass().getName()
                )
            );
        }
        return this.ihttpRequest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String message() {
        return this.imessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final UriBuilder base() {
        final UriBuilder builder = this.uriInfo()
            .getBaseUriBuilder()
            .clone();
        if (this.icookie != null && !this.icookie.isEmpty()
            && this.addAuthToURIs) {
            builder.queryParam(AbstractRs.AUTH_PARAM, this.icookie);
        }
        return builder;
    }

    /**
     * Inject message, if it was sent.
     * @param msg The message
     */
    @CookieParam("netbout-msg")
    public final void setMessage(final String msg) {
        if (msg != null) {
            String decoded;
            try {
                decoded = new String(new Base64().decode(msg), "UTF-8");
            } catch (java.io.UnsupportedEncodingException ex) {
                throw new IllegalArgumentException(ex);
            }
            this.imessage = decoded;
            Logger.debug(
                this,
                "#setMessage('%s'): injected as '%s'",
                msg,
                decoded
            );
        }
    }

    /**
     * Set cookie. Should be called by JAX-RS implemenation
     * because of <tt>&#64;CookieParam</tt> annotation.
     * @param cookie The cookie to set
     */
    @CookieParam(AbstractPage.AUTH_COOKIE)
    public final void setCookie(final String cookie) {
        if (cookie != null) {
            this.icookie = cookie;
            Logger.debug(
                this,
                "#setCookie('%s'): injected",
                cookie
            );
        }
    }

    /**
     * Set auth code. Should be called by JAX-RS implemenation
     * because of <tt>&#64;CookieParam</tt> annotation.
     * @param auth The auth code to set
     */
    @QueryParam(AbstractRs.AUTH_PARAM)
    public final void setAuth(final String auth) {
        if (auth != null) {
            this.icookie = auth;
            this.addAuthToURIs = true;
            Logger.debug(
                this,
                "#setAuth('%s'): injected",
                auth
            );
        }
    }

    /**
     * Set URI Info. Should be called by JAX-RS implemenation
     * because of <tt>&#64;Context</tt> annotation.
     * @param info The info to inject
     */
    @Context
    public final void setUriInfo(final UriInfo info) {
        this.iuriInfo = info;
        Logger.debug(
            this,
            "#setUriInfo(%s): injected",
            info.getClass().getName()
        );
    }

    /**
     * Set Providers. Should be called by JAX-RS implemenation
     * because of <tt>&#64;Context</tt> annotation.
     * @param prov List of providers
     */
    @Context
    public final void setProviders(final Providers prov) {
        this.iproviders = prov;
        Logger.debug(
            this,
            "#setProviders(%s): injected",
            prov.getClass().getName()
        );
    }

    /**
     * Set HttpHeaders. Should be called by JAX-RS implemenation
     * because of <tt>&#64;Context</tt> annotation.
     * @param hdrs List of headers
     */
    @Context
    public final void setHttpHeaders(final HttpHeaders hdrs) {
        this.ihttpHeaders = hdrs;
        Logger.debug(
            this,
            "#setHttpHeaders(%s): injected",
            hdrs.getClass().getName()
        );
    }

    /**
     * Set HttpServletRequest. Should be called by JAX-RS implemenation
     * because of <tt>&#64;Context</tt> annotation.
     * @param request The request
     */
    @Context
    public final void setHttpServletRequest(final HttpServletRequest request) {
        this.ihttpRequest = request;
        Logger.debug(
            this,
            "#setHttpServletRequest(%s): injected",
            request.getClass().getName()
        );
    }

    /**
     * Inject servlet context. Should be called by JAX-RS implemenation
     * because of <tt>&#64;Context</tt> annotation. Servlet attributes are
     * injected into context by {@link com.netbout.servlets.Starter} servlet
     * listener.
     * @param context The context
     */
    @Context
    public final void setServletContext(final ServletContext context) {
        this.ihub = (Hub) context.getAttribute("com.netbout.rest.HUB");
        this.ibus = (Bus) context.getAttribute("com.netbout.rest.BUS");
        Logger.debug(
            this,
            "#setServletContext(%s): injected",
            context.getClass().getName()
        );
    }

    /**
     * Get current user identity, or throws {@link LoginRequiredException} if
     * no user is logged in at the moment.
     * @return The identity
     */
    protected final Identity identity() {
        try {
            return new Cryptor().decrypt(this.ihub, this.icookie);
        } catch (com.netbout.utils.DecryptionException ex) {
            Logger.debug(
                this,
                "Decryption failure from %s calling '%s': %s",
                this.httpServletRequest().getRemoteAddr(),
                this.httpServletRequest().getRequestURI(),
                ex.getMessage()
            );
            throw new LoginRequiredException(this, ex);
        }
    }

    /**
     * Forget current identity, if it exists.
     */
    protected final void logoff() {
        this.icookie = "";
    }

    /**
     * Get bus.
     * @return The bus
     */
    protected final Bus bus() {
        if (this.ibus == null) {
            throw new IllegalStateException(
                String.format(
                    "%s#bus was never injected by container",
                    this.getClass().getName()
                )
            );
        }
        return this.ibus;
    }

    /**
     * Get hub.
     * @return The hub
     */
    protected final Hub hub() {
        if (this.ibus == null) {
            throw new IllegalStateException(
                String.format(
                    "%s#hub was never injected by container",
                    this.getClass().getName()
                )
            );
        }
        return this.ihub;
    }

    /**
     * Get base with auth token.
     * @return The builder
     */
    protected final UriBuilder baseWithToken() {
        return this.uriInfo()
            .getBaseUriBuilder()
            .queryParam(AbstractRs.AUTH_PARAM, this.icookie)
            .clone();
    }

}
