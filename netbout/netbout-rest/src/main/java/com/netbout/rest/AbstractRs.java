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
import com.netbout.spi.cpa.CpaHelper;
import com.ymock.util.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
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
     * Entry to work with.
     */
    private transient Entry ientry = new HubEntry();

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
    @Context
    private transient HttpHeaders ihttpHeaders;

    /**
     * HTTP servlet request.
     */
    @Context
    private transient HttpServletRequest ihttpRequest;

    /**
     * Cookie.
     */
    private transient String icookie;
    // Uncomment this line if you don't have a cookie saved by your
    // local browser yet.
    // = "Sm9obiBEb2U=.am9obm55LmRvZQ==.97febcab64627f2ebc4bb9292c3cc0bd";

    /**
     * The message to show.
     */
    private transient String imessage = "";

    /**
     * Send all JUL logging to SLF4J. Some libraries may use JUL for some
     * reason and we should configure it properly.
     */
    static {
        final java.util.logging.Logger rootLogger =
            java.util.logging.LogManager.getLogManager().getLogger("");
        final java.util.logging.Handler[] handlers =
            rootLogger.getHandlers();
        for (int idx = 0; idx < handlers.length; idx += 1) {
            rootLogger.removeHandler(handlers[idx]);
        }
        org.slf4j.bridge.SLF4JBridgeHandler.install();
    }

    /**
     * Register basic helper in a hub.
     */
    static {
        Identity persistor;
        try {
            // @checkstyle MultipleStringLiterals (1 line)
            persistor = new HubEntry().user("netbout").identity("nb:db");
        } catch (com.netbout.spi.DuplicateIdentityException ex) {
            throw new IllegalStateException(ex);
        }
        persistor.alias("Netbout Database Manager");
        try {
            persistor.promote(new CpaHelper("com.netbout.db"));
        } catch (com.netbout.spi.HelperException ex) {
            throw new IllegalStateException(ex);
        }
        try {
            persistor.setPhoto(
                new java.net.URL("http://img.netbout.com/db.png")
            );
        } catch (java.net.MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Initializer.
     */
    static {
        Identity hub;
        try {
            // @checkstyle MultipleStringLiterals (1 line)
            hub = new HubEntry().user("netbout").identity("nb:hh");
        } catch (com.netbout.spi.DuplicateIdentityException ex) {
            throw new IllegalStateException(ex);
        }
        hub.alias("Netbout Hub");
        try {
            hub.promote(new CpaHelper("com.netbout.hub.hh"));
        } catch (com.netbout.spi.HelperException ex) {
            throw new IllegalStateException(ex);
        }
        try {
            hub.setPhoto(
                new java.net.URL("http://img.netbout.com/hh.png")
            );
        } catch (java.net.MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Entry entry() {
        if (this.ientry == null) {
            throw new IllegalStateException(
                String.format(
                    "%s#entry was never injected by setEntry()",
                    this.getClass().getName()
                )
            );
        }
        return this.ientry;
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
     * Set new entry.
     * @param ent The entry to work with
     */
    public final void setEntry(final Entry ent) {
        this.ientry = ent;
        Logger.debug(
            this,
            "#setEntry('%s'): injected",
            this.ientry.getClass().getName()
        );
    }

    /**
     * Set cookie. Should be called by JAX-RS implemenation
     * because of <tt>&#64;CookieParam</tt> annotation.
     * @param cookie The cookie to set
     */
    @CookieParam("netbout")
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
     * Get current user identity, or throws {@link LoginRequiredException} if
     * no user is logged in at the moment.
     * @return The identity
     */
    protected final Identity identity() {
        try {
            return new Cryptor(this.entry()).decrypt(this.icookie);
        } catch (Cryptor.DecryptionException ex) {
            Logger.debug(
                this,
                "Decryption failure from %s calling '%s': %s",
                this.httpServletRequest().getRemoteAddr(),
                this.httpServletRequest().getRequestURI(),
                ex.getMessage()
            );
            throw new ForwardException(this, "/g", ex);
        }
    }

}
