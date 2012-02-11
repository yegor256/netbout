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

import com.netbout.hub.Hub;
import com.netbout.spi.Identity;
import com.netbout.spi.client.RestSession;
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
import org.apache.commons.lang.CharEncoding;

/**
 * Abstract RESTful resource.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
public abstract class AbstractRs implements Resource {

    /**
     * When this resource was started, in nanoseconds.
     */
    private final transient long inano = System.nanoTime();

    /**
     * Hub to work with.
     */
    private transient Hub ihub;

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
     * @todo #226 I think that we should cache this object somewhere here
     */
    @Override
    public final Identity identity() {
        try {
            return new Cryptor().decrypt(this.ihub, this.icookie);
        } catch (com.netbout.utils.DecryptionException ex) {
            Logger.debug(
                this,
                "Decryption failure from %s calling '%s': %[exception]s",
                this.httpServletRequest().getRemoteAddr(),
                this.httpServletRequest().getRequestURI(),
                ex
            );
            throw new LoginRequiredException(this, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Providers providers() {
        if (this.iproviders == null) {
            throw new IllegalStateException(
                Logger.format(
                    "%[type]s#providers was never injected by JAX-RS",
                    this
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
                Logger.format(
                    "%[type]s#httpHeaders was never injected by JAX-RS",
                    this
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
                Logger.format(
                    "%[type]s#uriInfo was never injected by JAX-RS",
                    this
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
                Logger.format(
                    "%[type]s#httpRequest was never injected by JAX-RS",
                    this
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
            builder.queryParam(RestSession.AUTH_PARAM, this.icookie);
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
                decoded = new String(
                    new Base64().decode(msg),
                    CharEncoding.UTF_8
                );
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
    @CookieParam(RestSession.AUTH_COOKIE)
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
    @QueryParam(RestSession.AUTH_PARAM)
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
            "#setUriInfo(%[type]s): injected",
            info
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
            "#setProviders(%[type]s): injected",
            prov
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
            "#setHttpHeaders(%[type]s): injected",
            hdrs
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
            "#setHttpServletRequest(%[type]s): injected",
            request
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
        if (this.ihub == null) {
            throw new IllegalStateException("HUB is not initialized");
        }
        com.netbout.notifiers.email.RoutineFarm.setHub(this.ihub);
        Logger.debug(
            this,
            "#setServletContext(%[type]s): injected",
            context
        );
    }

    /**
     * Initialize all fields from another resource.
     * @param res The parent resource
     * @return This object
     * @param <T> The type of it
     */
    public final <T> T duplicate(final Resource res) {
        this.ihub = ((AbstractRs) res).hub();
        this.setProviders(res.providers());
        this.setHttpHeaders(res.httpHeaders());
        this.setUriInfo(res.uriInfo());
        this.setHttpServletRequest(res.httpServletRequest());
        this.setMessage(res.message());
        if (((AbstractRs) res).addAuthToURIs) {
            this.setAuth(((AbstractRs) res).icookie);
        } else {
            this.setCookie(((AbstractRs) res).icookie);
        }
        return (T) this;
    }

    /**
     * Forget current identity, if it exists.
     */
    protected final void logoff() {
        this.icookie = "";
    }

    /**
     * Get hub.
     * @return The hub
     */
    protected final Hub hub() {
        if (this.ihub == null) {
            throw new IllegalStateException(
                Logger.format(
                    "%[type]s#hub was never injected by container",
                    this
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
            .queryParam(RestSession.AUTH_PARAM, this.icookie)
            .clone();
    }

}
