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
package com.netbout.rest.auth;

import com.netbout.rest.AbstractPage;
import com.netbout.rest.AbstractRs;
import com.netbout.rest.CookieBuilder;
import com.netbout.rest.Cryptor;
import com.netbout.rest.LoginRequiredException;
import com.netbout.rest.page.PageBuilder;
import com.netbout.spi.Identity;
import com.netbout.spi.Urn;
import com.netbout.spi.client.RestSession;
import com.netbout.spi.text.SecureString;
import com.ymock.util.Logger;
import java.net.URI;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * REST authentication page.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Path("/auth")
public final class AuthRs extends AbstractRs {

    /**
     * The URL to go next.
     */
    private transient URI forward;

    /**
     * Set goto URI.
     * @param uri The URI
     */
    @CookieParam(RestSession.GOTO_COOKIE)
    public void setGoto(final String uri) {
        if (uri != null) {
            try {
                this.forward = new URI(SecureString.valueOf(uri).text());
            } catch (com.netbout.spi.text.StringDecryptionException ex) {
                Logger.warn(
                    this,
                    "#setGoto('%s'): failed to decrypt: %[exception]s",
                    uri,
                    ex
                );
            } catch (java.net.URISyntaxException ex) {
                Logger.warn(
                    this,
                    "#setGoto('%s'): failed to create URI: %[exception]s",
                    uri,
                    ex
                );
            }
        }
    }

    /**
     * Authentication page.
     * @param iname Identity name
     * @param secret Secret word
     * @param path Where to go next
     * @return The JAX-RS response
     * @todo #158 Path annotation: http://java.net/jira/browse/JERSEY-739
     */
    @GET
    @Path("/")
    public Response auth(@QueryParam("identity") final Urn iname,
        @QueryParam("secret") final String secret,
        @QueryParam("goto") @DefaultValue("/") final String path) {
        if (iname == null || secret == null) {
            throw new LoginRequiredException(
                this,
                "'identity' and 'secret' query params are mandatory"
            );
        }
        final Identity identity = this.identity(iname, secret);
        URI location;
        if (this.forward == null) {
            location = this.base().path(path).build();
        } else {
            location = this.forward;
        }
        return new PageBuilder()
            .build(AbstractPage.class)
            .init(this)
            .authenticated(identity)
            .cookie(
                new CookieBuilder(this.base().build())
                    .named(RestSession.GOTO_COOKIE)
                    .build()
            )
            .status(Response.Status.SEE_OTHER)
            .location(location)
            .header(RestSession.AUTH_HEADER, new Cryptor().encrypt(identity))
            .build();
    }

    /**
     * Create identity.
     * @param iname Identity name
     * @param secret Secret word
     * @return The identity
     */
    private Identity identity(final Urn iname, final String secret) {
        Identity identity;
        try {
            final Identity previous = this.identity();
            this.logoff();
            identity = this.authenticate(iname, secret);
            if (AbstractPage.trusted(identity)
                && !AbstractPage.trusted(previous)) {
                identity = this.hub().join(identity, previous);
            } else if (AbstractPage.trusted(previous)
                && !AbstractPage.trusted(identity)) {
                identity = this.hub().join(previous, identity);
            } else if (identity.name().equals(previous.name())) {
                Logger.info(
                    this,
                    "Successfull re-authentication of '%s'",
                    identity.name()
                );
            } else {
                Logger.info(
                    this,
                    "Authentication of '%s' was replaced by '%s'",
                    previous.name(),
                    identity.name()
                );
            }
        } catch (LoginRequiredException ex) {
            identity = this.authenticate(iname, secret);
        }
        return identity;
    }

    /**
     * Authenticate the user through facebook.
     * @param iname Identity name
     * @param secret Secret word
     * @return The identity found
     */
    private Identity authenticate(final Urn iname,
        final String secret) {
        RemoteIdentity remote;
        try {
            remote = new AuthMediator(this.hub().resolver())
                .authenticate(iname, secret);
        } catch (java.io.IOException ex) {
            Logger.warn(this, "%[exception]s", ex);
            throw new LoginRequiredException(this, ex);
        }
        Identity identity;
        try {
            identity = remote.findIn(this.hub());
        } catch (com.netbout.spi.UnreachableUrnException ex) {
            throw new LoginRequiredException(this, ex);
        }
        return identity;
    }

}
