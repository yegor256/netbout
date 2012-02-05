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

import com.rexsl.test.RestTester;
import com.rexsl.test.TestClient;
import com.rexsl.test.TestResponse;
import java.net.URI;
import java.net.URLEncoder;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.hamcrest.Matchers;

/**
 * Client that loads XML through HTTP, using Jersey JAX-RS client.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class RexslRestClient implements RestClient {

    /**
     * Test client.
     */
    private final transient TestClient client;

    /**
     * Auth token.
     */
    private final transient String token;

    /**
     * Pubic ctor.
     * @param clnt The client
     * @param tkn Auth token
     */
    public RexslRestClient(final TestClient clnt, final String tkn) {
        this.client = clnt;
        assert tkn != null : "authentication token is mandatory";
        assert !tkn.isEmpty() : "token can't be empty";
        this.token = tkn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RestClient queryParam(final String name, final String value) {
        final URI uri = UriBuilder
            .fromUri(this.client.uri())
            .queryParam(name, "{value}")
            .build(value);
        return new RexslRestClient(
            RestTester.start(uri),
            this.token
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RestResponse get(final String message) {
        final TestResponse response = this.client
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
            .header(
                HttpHeaders.COOKIE,
                new Cookie(RestSession.AUTH_COOKIE, this.token)
            )
            .get(message)
            .assertHeader(RestSession.ERROR_HEADER, Matchers.nullValue())
            .assertThat(new EtaAssertion());
        return new RexslRestResponse(this, response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RestResponse post(final String message, final String... params) {
        final StringBuilder data = new StringBuilder();
        for (int pos = 0; pos < params.length; pos += 2) {
            if (pos > 0) {
                data.append("&");
            }
            data.append(URLEncoder.encode(params[pos]))
                .append("=")
                .append(URLEncoder.encode(params[pos + 1]));
        }
        final TestResponse response = this.client
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
            .header(
                HttpHeaders.COOKIE,
                new Cookie(RestSession.AUTH_COOKIE, this.token)
            )
            .post(message, data.toString())
            .assertHeader(RestSession.ERROR_HEADER, Matchers.nullValue());
        return new RexslRestResponse(this, response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RestClient copy() {
        return new RexslRestClient(RestTester.start(this.uri()), this.token);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RestClient copy(final URI uri) {
        return new RexslRestClient(RestTester.start(uri), this.token);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RestClient copy(final String uri) {
        return new RexslRestClient(
            RestTester.start(UriBuilder.fromUri(uri).build()),
            this.token
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI uri() {
        return UriBuilder
            .fromUri(this.client.uri())
            .queryParam(RestSession.AUTH_PARAM, this.token)
            .build();
    }

}
