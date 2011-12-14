/**
 * Copyright (c) 2009-2011, NetBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi.client;

import com.netbout.spi.Identity;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.ymock.util.Logger;
import java.net.HttpURLConnection;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;

/**
 * Restful session.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class RestSession {

    /**
     * Home URI.
     */
    private final transient URI home;

    /**
     * Home URI.
     */
    private final transient Client client;

    /**
     * Public ctor.
     * @param uri Home URI
     */
    public RestSession(final URI uri) {
        if (!uri.isAbsolute()) {
            throw new IllegalArgumentException(
                String.format(
                    "URI '%s' has to be absolute",
                    uri
                )
            );
        }
        this.home = UriBuilder.fromUri(uri).path("/").build();
        final ClientConfig config = new DefaultClientConfig();
        config.getProperties()
            .put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, false);
        this.client = Client.create(config);
    }

    /**
     * Get identity in the session through Netbout authentication mechanism.
     * @param iname Name of the identity
     * @param secret The secret word to use
     * @return The identity to work with
     */
    public Identity authenticate(final String iname, final String secret) {
        return new RestIdentity(
            new JerseyRestClient(
                this.client.resource(this.home),
                this.fetch(iname, secret)
            )
        );
    }

    /**
     * Fetch auth code.
     * @param identity Name of the identity
     * @param secret The secret word to use
     * @return The URL
     */
    private String fetch(final String identity, final String secret) {
        final WebResource resource = this.client.resource(this.home)
            .path("/auth")
            .queryParam("identity", identity)
            .queryParam("secret", secret);
        final ClientResponse response = resource.get(ClientResponse.class);
        if (response.getStatus() != HttpURLConnection.HTTP_SEE_OTHER) {
            throw new IllegalArgumentException(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "Invalid HTTP status %d at %s during authentication of '%s'",
                    response.getStatus(),
                    resource.getURI(),
                    identity
                )
            );
        }
        final String token = response.getHeaders().getFirst("Netbout-auth");
        if (token == null) {
            throw new IllegalArgumentException(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "Authentication token not found in response header at '%s' during authentication of '%s'",
                    resource.getURI(),
                    identity
                )
            );
        }
        Logger.debug(
            this,
            "#fetch('%s', '%s'): '%s' authenticated us as '%s'",
            identity,
            secret,
            resource.getURI(),
            token
        );
        return token;
    }

}
