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

import com.netbout.hub.Hub;
import com.netbout.spi.Identity;
import com.netbout.spi.IdentityMocker;
import com.netbout.utils.Cryptor;
import com.rexsl.core.XslResolver;
import com.rexsl.test.XhtmlConverter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Source;
import org.hamcrest.MatcherAssert;
import org.mockito.Mockito;
import org.xmlmatchers.XmlMatchers;

/**
 * Builds an instance of {@link UriInfo}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class UriInfoMocker {

    /**
     * Request URI.
     */
    private transient URI uri;

    /**
     * Public ctor.
     * @throws Exception If something is wrong
     */
    public UriInfoMocker() throws Exception {
        this.uri = new URI("http://localhost:99/local");
    }

    /**
     * With this request URI.
     * @param ruri The URI
     * @return This object
     */
    public UriInfoMocker withRequestUri(final URI ruri) {
        this.uri = ruri;
        return this;
    }

    /**
     * Build an instance of provided class.
     * @return The resource just created
     * @throws Exception If something is wrong
     */
    public UriInfo mock() throws Exception {
        final UriInfo info = Mockito.mock(UriInfo.class);
        Mockito.doReturn(this.uri).when(info).getRequestUri();
        Mockito.doReturn(UriBuilder.fromUri(this.uri))
            .when(info).getBaseUriBuilder();
        Mockito.doReturn(this.uri).when(info).getAbsolutePath();
        Mockito.doReturn(this.uri).when(info).getBaseUri();
        return info;
    }

}
