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
package com.netbout.rest;

import com.jcabi.log.Logger;
import com.netbout.hub.Hub;
import com.netbout.rest.log.LogList;
import com.netbout.spi.Identity;
import com.netbout.spi.client.RestSession;
import com.netbout.spi.text.SecureString;
import com.rexsl.page.BaseResource;
import java.net.URI;
import java.util.Locale;
import javax.servlet.ServletContext;
import javax.ws.rs.CookieParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * Abstract RESTful resource.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public class BaseRs extends BaseResource implements NbResource {

    /**
     * List of log events.
     */
    private final transient LogList loglist = new LogList();

    /**
     * Hub to work with.
     */
    private transient Hub ihub;

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
    public final LogList log() {
        return this.loglist;
    }

    /**
     * {@inheritDoc}
     * @todo #226 I think that we should cache this object somewhere here
     */
    @Override
    public final Identity identity() {
        Identity identity;
        try {
            identity = CryptedIdentity.parse(this.ihub, this.icookie);
        } catch (CryptedIdentity.DecryptionException ex) {
            Logger.debug(
                this,
                "Decryption failure from %s calling '%s': %[exception]s",
                this.httpServletRequest().getRemoteAddr(),
                this.httpServletRequest().getRequestURI(),
                ex
            );
            throw new LoginRequiredException(this, ex);
        }
        final String https = "https";
        final URI base = this.uriInfo().getBaseUri();
        if (!https.equals(base.getScheme().toLowerCase(Locale.ENGLISH))
            && !"localhost".equals(base.getHost())) {
            throw new WebApplicationException(
                Response.status(Response.Status.TEMPORARY_REDIRECT).location(
                    this.uriInfo().getRequestUriBuilder()
                        .clone()
                        .scheme(https)
                        .build()
                ).build()
            );
        }
        return identity;
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
     *
     * @see http://java.net/jira/browse/JERSEY-1081
     */
    @Override
    public final UriBuilder base() {
        final UriBuilder builder = this.uriInfo()
            .getBaseUriBuilder()
            .clone();
        final String qauth = this.qauth();
        if (!qauth.isEmpty()) {
            builder.queryParam(RestSession.AUTH_PARAM, qauth);
        }
        return UriBuilder.fromUri(builder.build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String qauth() {
        String qauth;
        if (this.icookie == null || this.icookie.isEmpty()
            || !this.addAuthToURIs) {
            qauth = "";
        } else {
            qauth = this.icookie;
        }
        return qauth;
    }

    /**
     * Inject message, if it was sent.
     * @param msg The message
     */
    @CookieParam("netbout-msg")
    public final void setMessage(final String msg) {
        if (msg != null) {
            try {
                this.imessage = SecureString.valueOf(msg).text();
            } catch (com.netbout.spi.text.StringDecryptionException ex) {
                this.imessage = ex.getMessage();
            }
            Logger.debug(
                this,
                "#setMessage('%s'): injected as '%s'",
                msg,
                this.imessage
            );
        }
    }

    /**
     * Set list of log events from previous page rendering.
     * Should be called by JAX-RS implemenation
     * because of <tt>&#64;CookieParam</tt> annotation.
     * @param text Packed text
     */
    @CookieParam(RestSession.LOG_COOKIE)
    public final void setLog(final String text) {
        if (text != null) {
            this.loglist.append(text);
            Logger.debug(
                this,
                "#setLog('%s'): injected",
                text
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
            this.setCookie(auth);
            this.addAuthToURIs = true;
        }
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
    @SuppressWarnings("unchecked")
    public final <T> T duplicate(final BaseRs res) {
        this.ihub = res.hub();
        this.setProviders(res.providers());
        this.setHttpHeaders(res.httpHeaders());
        this.setUriInfo(res.uriInfo());
        this.setHttpServletRequest(res.httpServletRequest());
        this.setMessage(res.message());
        if (res.addAuthToURIs) {
            this.setAuth(res.icookie);
        } else {
            this.setCookie(res.icookie);
        }
        return (T) this;
    }

    /**
     * Forget current identity, if it exists.
     */
    protected final void logoff() {
        this.setCookie("");
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

}
