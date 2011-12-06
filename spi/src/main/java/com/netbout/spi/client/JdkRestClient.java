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

import java.net.URI;
import java.util.List;

/**
 * Client that loads XML through HTTP, using JDK.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class JdkRestClient implements RestClient {

    /**
     * Pubic ctor.
     * @param uri Entry point URI
     * @param auth Authentication token
     */
    public JdkRestClient(final URI uri, final String auth) {
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
        return new DefaultRestResponse();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RestClient clone() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RestClient clone(final URI uri) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RestClient clone(final String uri) {
        return this;
    }

}
