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
import com.netbout.spi.Urn;
import com.rexsl.test.RestTester;
import com.ymock.util.Logger;
import java.net.HttpURLConnection;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import org.hamcrest.Matchers;

/**
 * Restful session.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class RestSession {

    /**
     * Authentication header.
     */
    public static final String AUTH_HEADER = "Netbout-auth";

    /**
     * HTTP header with error message.
     */
    public static final String ERROR_HEADER = "Netbout-error";

    /**
     * Authentication query param.
     */
    public static final String AUTH_PARAM = "auth";

    /**
     * Name of the user authentication cookie.
     */
    public static final String AUTH_COOKIE = "netbout";

    /**
     * Name of the message transferring cookie.
     */
    public static final String MESSAGE_COOKIE = "netbout-msg";

    /**
     * Query param to search INBOX.
     */
    public static final String QUERY_PARAM = "q";

    /**
     * Home URI.
     */
    private final transient URI home;

    /**
     * Public ctor.
     * @param uri Home URI
     */
    public RestSession(final URI uri) {
        if (!uri.isAbsolute()) {
            throw new IllegalArgumentException(
                Logger.format(
                    "URI '%s' has to be absolute",
                    uri
                )
            );
        }
        this.home = UriBuilder.fromUri(uri).path("/").build();
    }

    /**
     * Get identity in the session through Netbout authentication mechanism.
     * @param iname Name of the identity
     * @param secret The secret word to use
     * @return The identity to work with
     */
    public Identity authenticate(final Urn iname, final String secret) {
        return new RestIdentity(
            new RexslRestClient(
                RestTester.start(this.home),
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
    private String fetch(final Urn identity, final String secret) {
        final URI uri = UriBuilder.fromUri(this.home)
            .path("/auth")
            .queryParam("identity", identity.toString())
            .queryParam("secret", secret)
            .build();
        final String token = RestTester.start(uri)
            .get("authorization")
            .assertStatus(HttpURLConnection.HTTP_SEE_OTHER)
            .assertHeader(this.AUTH_HEADER, Matchers.notNullValue())
            .assertHeader(this.AUTH_HEADER, Matchers.not(Matchers.empty()))
            .getHeaders()
            .getFirst(this.AUTH_HEADER);
        Logger.debug(
            this,
            "#fetch('%s', '%s'): '%s' authenticated us as '%s'",
            identity,
            secret,
            uri,
            token
        );
        return token;
    }

}
