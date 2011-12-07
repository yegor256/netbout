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

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.ymock.util.Logger;
import java.net.URI;
import java.util.List;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

/**
 * Client that loads XML through HTTP, using Jersey JAX-RS client.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class JerseyRestClient implements RestClient {

    /**
     * Jersey web resource.
     */
    private final transient WebResource resource;

    /**
     * Auth token.
     */
    private final transient String token;

    /**
     * Pubic ctor.
     * @param res The resource to work with
     * @param tkn Auth token
     */
    public JerseyRestClient(final WebResource res, final String tkn) {
        this.resource = res;
        this.token = tkn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RestClient queryParam(final String name, final String value) {
        return new JerseyRestClient(
            this.resource.queryParam(name, value),
            this.token
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RestResponse get(final String message) {
        final long start = System.currentTimeMillis();
        final ClientResponse response = this.resource
            .accept(MediaType.APPLICATION_XML)
            .cookie(this.cookie())
            .get(ClientResponse.class);
        Logger.info(
            this,
            "#GET(%s): \"%s\" [%d %s] in %dms (%s)",
            this.resource.getURI().getPath(),
            message,
            response.getStatus(),
            response.getClientResponseStatus().getReasonPhrase(),
            System.currentTimeMillis() - start,
            this.resource.getURI()
        );
        return new JerseyRestResponse(this, response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RestResponse post(final String message, final String... params) {
        final MultivaluedMap data = new MultivaluedMapImpl();
        for (int pos = 0; pos < params.length; pos += 1) {
            data.add(params[pos], params[pos + 1]);
            pos += 1;
        }
        final long start = System.currentTimeMillis();
        final ClientResponse response = this.resource
            .cookie(this.cookie())
            .post(ClientResponse.class, data);
        Logger.info(
            this,
            "#POST(%s): \"%s\" [%d %s] in %dms (%s)",
            this.resource.getURI().getPath(),
            message,
            response.getStatus(),
            response.getClientResponseStatus().getReasonPhrase(),
            System.currentTimeMillis() - start,
            this.resource.getURI()
        );
        return new JerseyRestResponse(this, response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RestClient copy() {
        return new JerseyRestClient(this.resource, this.token);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RestClient copy(final URI uri) {
        return new JerseyRestClient(this.resource.uri(uri), this.token);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RestClient copy(final String uri) {
        return new JerseyRestClient(
            this.resource.uri(UriBuilder.fromUri(uri).build()),
            this.token
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI uri() {
        return UriBuilder.fromUri(this.resource.getURI())
            .queryParam("auth", this.token)
            .build();
    }

    /**
     * Make cookie.
     * @return The cookie
     */
    private Cookie cookie() {
        return new Cookie("netbout", this.token);
    }

}
