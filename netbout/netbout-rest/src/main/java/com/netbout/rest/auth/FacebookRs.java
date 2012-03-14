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
import com.netbout.rest.LoginRequiredException;
import com.netbout.rest.page.PageBuilder;
import com.netbout.spi.Identity;
import com.netbout.spi.Urn;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.types.User;
import com.rexsl.core.Manifests;
import com.ymock.util.Logger;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.LocaleUtils;

/**
 * Facebook authentication page.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @see <a href="http://developers.facebook.com/docs/authentication/">facebook.com</a>
 */
@Path("/fb")
public final class FacebookRs extends AbstractRs {

    /**
     * Namespace.
     */
    public static final String NAMESPACE = "facebook";

    /**
     * Super secret code.
     */
    @SuppressWarnings("PMD.DefaultPackage")
    static final String SUPER_SECRET =
        "PR45-IU6Y-23ER-9IMW-PAQ2-OO6T-EF5G-PLM6";

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
            .build(AbstractPage.class)
            .init(this)
            .preserved()
            .status(Response.Status.SEE_OTHER)
            .location(
                this.base().path("/auth")
                    .queryParam("identity", new Urn(this.NAMESPACE, ""))
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
    public Response auth(@QueryParam("identity") final Urn iname,
        @QueryParam("secret") final String secret) {
        if (iname == null || secret == null) {
            throw new LoginRequiredException(
                this,
                "'identity' and 'secret' query params are mandatory"
            );
        }
        if (!this.NAMESPACE.equals(iname.nid())) {
            throw new LoginRequiredException(
                this,
                String.format(
                    "NID '%s' is not correct in '%s', '%s' expected",
                    iname.nid(),
                    iname,
                    this.NAMESPACE
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
            .build(AbstractPage.class)
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
        final User fbuser = this.user(code);
        final Identity resolved = new ResolvedIdentity(
            UriBuilder.fromUri("http://www.netbout.com/fb").build().toURL(),
            new Urn(this.NAMESPACE, fbuser.getId())
        );
        resolved.profile().setPhoto(
            UriBuilder
                .fromPath("https://graph.facebook.com/{id}/picture")
                .build(fbuser.getId())
                .toURL()
        );
        resolved.profile().alias(fbuser.getName());
        resolved.profile().setLocale(LocaleUtils.toLocale(fbuser.getLocale()));
        return resolved;
    }

    /**
     * Authenticate the user through facebook, and return its object.
     * @param code Facebook "authorization code"
     * @return The user
     * @throws IOException If some problem with FB
     */
    private User user(final String code) throws IOException {
        User fbuser;
        if (code.startsWith(this.SUPER_SECRET)) {
            fbuser = new User() {
                @Override
                public String getName() {
                    return "";
                }
                @Override
                public String getId() {
                    return code.substring(code.lastIndexOf('-') + 1);
                }
            };
        } else {
            fbuser = this.fbUser(this.token(code));
            assert fbuser != null;
        }
        return fbuser;
    }

    /**
     * Retrieve facebook access token.
     * @param code Facebook "authorization code"
     * @return The token
     * @throws IOException If some problem with FB
     */
    private String token(final String code) throws IOException {
        final String response = this.retrieve(
            UriBuilder
                // @checkstyle MultipleStringLiterals (5 lines)
                .fromPath("https://graph.facebook.com/oauth/access_token")
                .queryParam("client_id", "{id}")
                .queryParam("redirect_uri", "{uri}")
                .queryParam("client_secret", "{secret}")
                .queryParam("code", "{code}")
                .build(
                    Manifests.read("Netbout-FbId"),
                    this.base().path("/fb/back").build(),
                    Manifests.read("Netbout-FbSecret"),
                    code
                )
        );
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

    /**
     * Retrive data from the URL through HTTP request.
     * @param uri The URI
     * @return The response, text body
     * @throws IOException If some problem with FB
     */
    private String retrieve(final URI uri) throws IOException {
        final long start = System.nanoTime();
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) uri.toURL().openConnection();
        } catch (java.net.MalformedURLException ex) {
            throw new IOException(ex);
        }
        try {
            return IOUtils.toString(conn.getInputStream());
        } catch (java.io.IOException ex) {
            throw new IllegalArgumentException(ex);
        } finally {
            conn.disconnect();
            Logger.debug(
                this,
                "#retrieve(%s): done [%d] in %[nano]s",
                uri,
                conn.getResponseCode(),
                System.nanoTime() - start
            );
        }
    }

}
