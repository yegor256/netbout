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

import com.sun.jersey.api.client.WebResource;
import java.net.URI;
import java.util.List;
import javax.ws.rs.core.UriBuilder;

/**
 * Client that loads XML through HTTP, using JDK.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class JdkRestClient implements RestClient {

    /**
     * Jersey web resource.
     */
    private final transient WebResource;

    /**
     * Auth token.
     */
    private final transient String token;

    /**
     * Pubic ctor.
     * @param uri Entry point URI
     * @param auth Authentication token
     */
    public JdkRestClient(final URI uri, final String auth) {
        this.home = uri;
        this.token = auth;
        this.client = (HttpURLConnection) uri.toURL().openConnection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RestClient queryParam(final String name, final String value) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RestClient formParam(final String name, final String value) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RestResponse fetch(final String method) {
        final long start = System.currentTimeMillis();
        HttpURLConnection conn;
        int code;
        try {
            conn =
            code = conn.getResponseCode();
            conn.disconnect();
        } catch (java.io.IOException ex) {
            throw new IllegalArgumentException(ex);
        }
        if (code != HttpURLConnection.HTTP_OK) {
            throw new IllegalArgumentException(
                String.format(
                    "HTTP response code at '%s' is %d",
                    url,
                    code
                )
            );
        }
        final String auth = conn.getHeaderField("Netbout-auth");
        if (auth == null) {
            throw new IllegalArgumentException(
                "Netbout-auth header not found in response"
            );
        }
        Logger.debug(
            this,
            "#fetch('%s'): done in %dms",
            url,
            System.currentTimeMillis() - start
        );
        return auth;
        return new DefaultRestResponse();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RestClient clone() {
        return new JdkRestClient(this.home, this.token);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RestClient clone(final URI uri) {
        return new JdkRestClient(uri, this.token);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RestClient clone(final String uri) {
        return new JdkRestClient(UriBuilder.fromUri(uri).build(), this.token);
    }

}
