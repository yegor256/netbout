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

import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.mockito.Mockito;

/**
 * Builds an instance of {@link UriInfo}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class UriInfoMocker {

    /**
     * The mock.
     */
    private final transient UriInfo info = Mockito.mock(UriInfo.class);

    /**
     * Public ctor.
     */
    public UriInfoMocker() {
        this.withRequestUri(
            UriBuilder.fromUri("http://localhost:99/local").build()
        );
    }

    /**
     * With this request URI.
     * @param uri The URI
     * @return This object
     */
    public UriInfoMocker withRequestUri(final URI uri) {
        Mockito.doReturn(uri).when(this.info).getRequestUri();
        Mockito.doReturn(UriBuilder.fromUri(uri))
            .when(this.info).getBaseUriBuilder();
        Mockito.doReturn(UriBuilder.fromUri(uri))
            .when(this.info).getAbsolutePathBuilder();
        Mockito.doReturn(uri).when(this.info).getAbsolutePath();
        Mockito.doReturn(uri).when(this.info).getBaseUri();
        return this;
    }

    /**
     * Build an instance of provided class.
     * @return The resource just created
     */
    public UriInfo mock() {
        return this.info;
    }

}
