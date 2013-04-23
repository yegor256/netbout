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
package com.netbout.rest.auth;

import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import com.jcabi.urn.URN;
import com.netbout.rest.BaseRs;
import com.netbout.rest.LoginRequiredException;
import com.netbout.rest.NbPage;
import com.netbout.spi.Identity;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.types.User;
import com.rexsl.page.PageBuilder;
import com.rexsl.test.RestTester;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.lang3.LocaleUtils;

/**
 * Facebook authentication page.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @see <a href="http://developers.facebook.com/docs/authentication/">facebook.com</a>
 */
@Path("/fb")
public final class FacebookRs extends BaseRs {

    /**
     * Namespace.
     */
    public static final String NAMESPACE = "facebook";

    /**
     * Facebook authentication page (callback hits it).
     * @param code Facebook "authorization code"
     * @return The JAX-RS response
     */
    @GET
    @Path("/back")
    public Response fbauth(@QueryParam("code") final String code) {
        if (code == null) {
            throw new LoginRequiredException(
                this,
                "'code' is a mandatory query param"
            );
        }
        return new PageBuilder()
            .build(NbPage.class)
            .init(this)
            .preserved()
            .status(Response.Status.SEE_OTHER)
            .location(
                this.base().path("/auth")
                    .queryParam("identity", new URN(FacebookRs.NAMESPACE, ""))
                    .queryParam("secret", "{fbcode}")
                    .build(code)
            )
            .build();
    }

    /**
     * Authentication page.
     * @param iname Name of identity
     * @param secret The secret code
     * @return The JAX-RS response
     * @todo #158 Path annotation: http://java.net/jira/browse/JERSEY-739
     */
    @GET
    @Path("/")
    public Response auth(@QueryParam("identity") final URN iname,
        @QueryParam("secret") final String secret) {
        if (iname == null || secret == null) {
            throw new LoginRequiredException(
                this,
                "'identity' and 'secret' query params are mandatory"
            );
        }
        if (!FacebookRs.NAMESPACE.equals(iname.nid())) {
            throw new LoginRequiredException(
                this,
                String.format(
                    "NID '%s' is not correct in '%s', '%s' expected",
                    iname.nid(),
                    iname,
                    FacebookRs.NAMESPACE
                )
            );
        }
        Identity identity;
        try {
            identity = this.authenticate(secret);
        } catch (IOException ex) {
            Logger.warn(
                this,
                "Failed to auth at facebook (secret='%s'): %[exception]s",
                secret,
                ex
            );
            throw new LoginRequiredException(this, ex);
        }
        Logger.debug(
            this,
            "#auth('%s', '%s'): authenticated",
            iname,
            secret
        );
        return new PageBuilder()
            .build(NbPage.class)
            .init(this)
            .render()
            .authenticated(identity)
            .build();
    }

    /**
     * Authenticate the user through facebook.
     * @param code Facebook "authorization code"
     * @return The identity found
     * @throws IOException If some problem with FB
     */
    private Identity authenticate(final String code)
        throws IOException {
        final User fbuser = this.fbUser(this.token(code));
        final Identity resolved = new ResolvedIdentity(
            this.base().path("/fb").build().toURL(),
            new URN(FacebookRs.NAMESPACE, fbuser.getId())
        );
        resolved.profile().setPhoto(
            UriBuilder
                .fromUri("https://graph.facebook.com/")
                .path("/{id}/picture")
                .build(fbuser.getId())
                .toURL()
        );
        resolved.profile().alias(fbuser.getName());
        resolved.profile().setLocale(LocaleUtils.toLocale(fbuser.getLocale()));
        return resolved;
    }

    /**
     * Retrieve facebook access token.
     * @param code Facebook "authorization code"
     * @return The token
     * @throws IOException If some problem with FB
     */
    private String token(final String code) throws IOException {
        final URI uri = UriBuilder
            // @checkstyle MultipleStringLiterals (5 lines)
            .fromUri("https://graph.facebook.com/oauth/access_token")
            .queryParam("client_id", "{id}")
            .queryParam("redirect_uri", "{uri}")
            .queryParam("client_secret", "{secret}")
            .queryParam("code", "{code}")
            .build(
                Manifests.read("Netbout-FbId"),
                this.base().path("/fb/back").scheme("https").build(),
                Manifests.read("Netbout-FbSecret"),
                code
            );
        final String response = RestTester.start(uri)
            .get("getting access_token from Facebook")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .getBody();
        final String[] sectors = response.split("&");
        String token = null;
        for (String sector : sectors) {
            final String[] pair = sector.split("=");
            if (pair.length != 2) {
                throw new IOException(
                    String.format(
                        "Invalid response: '%s'",
                        response
                    )
                );
            }
            if ("access_token".equals(pair[0])) {
                token = pair[1];
                break;
            }
        }
        if (token == null) {
            throw new IOException(
                String.format(
                    "Access token not found in response: '%s'",
                    response
                )
            );
        }
        Logger.debug(
            this,
            "#token(..): found '%s'",
            token
        );
        return token;
    }

    /**
     * Get user name from Facebook, but the code provided.
     * @param token Facebook access token
     * @return The user found in FB
     * @throws IOException If some problem with FB
     */
    private User fbUser(final String token)
        throws IOException {
        try {
            final FacebookClient client = new DefaultFacebookClient(token);
            return client.fetchObject("me", com.restfb.types.User.class);
        } catch (com.restfb.exception.FacebookException ex) {
            throw new IOException(ex);
        }
    }

}
