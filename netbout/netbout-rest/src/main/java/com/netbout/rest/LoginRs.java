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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netbout.rest.page.JaxbBundle;
import com.netbout.rest.page.PageBuilder;
import com.netbout.spi.Identity;
import com.netbout.spi.User;
import com.rexsl.core.Manifests;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.io.IOUtils;

/**
 * RESTful front of login functions.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Path("/g")
public final class LoginRs extends AbstractRs {

    /**
     * Login page.
     * @return The page, convertable to XML
     * @see <a href="http://developers.facebook.com/docs/authentication/">facebook.com</a>
     */
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Page login() {
        final URI facebookUri = UriBuilder
            .fromPath("https://www.facebook.com/dialog/oauth")
            // @checkstyle MultipleStringLiterals (3 lines)
            .queryParam("client_id", Manifests.read("Netbout-FbId"))
            .queryParam(
                "redirect_uri",
                this.uriInfo().getAbsolutePathBuilder()
                    .replacePath("/g/fb")
                    .build()
            )
            .queryParam("scope", "email")
            .build();
        return new PageBuilder()
            .stylesheet("login")
            .build(AbstractPage.class)
            .init(this)
            .append(
                new JaxbBundle("facebook").attr(Page.HATEOAS_HREF, facebookUri)
            );
    }

    /**
     * Facebook authentication page (callback hits it).
     * @param code Facebook "authorization code"
     * @return The response
     */
    @Path("/fb")
    @GET
    public Response fbauth(@PathParam("code") final String code) {
        return new PageBuilder()
            .stylesheet("fbauth")
            .build(AbstractPage.class)
            .init(this)
            .authenticated(this.authenticate(code))
            .entity("")
            .status(Response.Status.TEMPORARY_REDIRECT)
            .location(UriBuilder.fromPath("/").build())
            .build();
    }

    /**
     * Authenticate the user through facebook.
     * @param code Facebook "authorization code"
     * @return The user found
     */
    private Identity authenticate(final String code) {
        final String name = this.retrieveUserName(code);
        final User user = this.entry().user(name);
        return user.identity(name);
    }

    /**
     * Get user name from Facebook, but the code provided.
     * @param code Facebook "authorization code"
     * @return The user name
     */
    private String retrieveUserName(final String code) {
        final String token = this.retrieve(
            UriBuilder
                // @checkstyle MultipleStringLiterals (5 lines)
                .fromPath("https://graph.facebook.com/oauth/access_token")
                .queryParam("client_id", Manifests.read("Netbout-FbId"))
                .queryParam("redirect_uri", this.uriInfo().getAbsolutePath())
                .queryParam("client_secret", Manifests.read("Netbout-FbSecret"))
                .queryParam("code", code)
                .build()
        );
        final String json = this.retrieve(
            UriBuilder
                .fromPath("https://graph.facebook.com/me")
                .replaceQuery(token)
                .build()
        );
        final Gson gson = new Gson();
        final Map<String, String> map = gson.fromJson(
            json, new LoginRs.JsonType().getType()
        );
        return map.get("name");
    }

    /**
     * Retrive data from the URL through HTTP request.
     * @param uri The URI
     * @return The response, text body
     */
    private String retrieve(final URI uri) {
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) uri.toURL().openConnection();
        } catch (java.net.MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        } catch (java.io.IOException ex) {
            throw new IllegalArgumentException(ex);
        }
        try {
            return IOUtils.toString(conn.getInputStream());
        } catch (java.io.IOException ex) {
            throw new IllegalArgumentException(ex);
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Supplementary type.
     * @see #retrieveUserName(String)
     */
    private static final class JsonType
        extends TypeToken<Map<String, String>> {
    }

}
