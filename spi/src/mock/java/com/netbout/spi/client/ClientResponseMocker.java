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
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import org.mockito.Mockito;

/**
 * Mocker of {@link ClientResponse}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class ClientResponseMocker {

    /**
     * Paths.
     */
    private final transient ClientResponse response =
        Mockito.mock(ClientResponse.class);

    /**
     * Headers.
     */
    private final transient MultivaluedMap headers = new MultivaluedMapImpl();

    /**
     * Return this status code.
     * @param code The code
     * @return This object
     */
    public ClientResponseMocker withStatus(final int code) {
        Mockito.doReturn(code).when(this.response).getStatus();
        return this;
    }

    /**
     * Return this header.
     * @param name The name of ti
     * @param value The value
     * @return This object
     */
    public ClientResponseMocker withHeader(final String name,
        final String value) {
        if (!this.headers.containsKey(name)) {
            this.headers.put(name, new ArrayList<String>());
        }
        ((List) this.headers.get(name)).add(value);
        return this;
    }

    /**
     * Return this entity.
     * @param entity The entity
     * @return This object
     */
    public ClientResponseMocker withEntity(final String entity) {
        Mockito.doReturn(entity).when(this.response)
            .getEntity(Mockito.any(Class.class));
        return this;
    }

    /**
     * Mock it.
     * @return Mocked class
     */
    public ClientResponse mock() {
        Mockito.doReturn(this.headers).when(this.response).getHeaders();
        return this.response;
    }

}
