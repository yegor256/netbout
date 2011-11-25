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
package com.netbout.harness;

import com.netbout.hub.HubEntry;
import com.netbout.rest.AbstractRs;
import com.netbout.rest.Resource;
import com.netbout.spi.Identity;
import com.netbout.utils.Cryptor;
import com.rexsl.core.XslResolver;
import java.net.URI;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.Marshaller;
import org.mockito.Mockito;

/**
 * Builds an instance of {@link Resource}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class ResourceBuilder {

    /**
     * User name.
     */
    public static final String USER = "John Turturro";

    /**
     * Identity name.
     */
    public static final String IDENTITY = "73640107";

    /**
     * Providers.
     */
    private final transient Providers iproviders =
        Mockito.mock(Providers.class);

    /**
     * URI info.
     */
    private final transient UriInfo iuriInfo =
        Mockito.mock(UriInfo.class);

    /**
     * Http headers.
     */
    private final transient HttpHeaders ihttpHeaders =
        Mockito.mock(HttpHeaders.class);

    /**
     * Servlet request.
     */
    private final transient HttpServletRequest ihttpRequest =
        Mockito.mock(HttpServletRequest.class);

    /**
     * Public ctor.
     * @throws Exception If something is wrong
     */
    public ResourceBuilder() throws Exception {
        final URI home = new URI("http://localhost:99/local");
        Mockito.doReturn(UriBuilder.fromUri(home))
            .when(this.iuriInfo).getBaseUriBuilder();
        Mockito.doReturn(home)
            .when(this.iuriInfo).getAbsolutePath();
        Mockito.doReturn(home)
            .when(this.iuriInfo).getBaseUri();
        Mockito.doReturn("localhost").when(this.ihttpRequest)
            .getRemoteAddr();
        Mockito.doReturn("/").when(this.ihttpRequest)
            .getRequestURI();
        Mockito.doReturn("/local").when(this.ihttpRequest)
            .getContextPath();
        Mockito.doReturn(new XslResolver())
            .when(this.iproviders)
            .getContextResolver(
                Marshaller.class, MediaType.APPLICATION_XML_TYPE
            );
    }

    /**
     * Build an instance of provided class.
     * @param type The class to build
     * @param <T> The class of response
     * @return The resource just created
     * @throws Exception If something is wrong
     */
    public <T> T build(final Class<? extends Resource> type) throws Exception {
        // @checkstyle IllegalType (1 line)
        final AbstractRs rest = (AbstractRs) type.newInstance();
        final Identity identity = HubEntry.user(this.USER)
            .identity(this.IDENTITY);
        identity.setPhoto(new URL("http://localhost/image.png"));
        rest.setUriInfo(this.uriInfo());
        rest.setHttpHeaders(this.httpHeaders());
        rest.setHttpServletRequest(this.httpServletRequest());
        rest.setProviders(this.providers());
        rest.setCookie(new Cryptor().encrypt(identity));
        return (T) rest;
    }

    /**
     * Create mock {@link UriInfo}.
     * @return The UriInfo object
     * @throws Exception If there is some problem inside
     */
    public UriInfo uriInfo() throws Exception {
        return this.iuriInfo;
    }

    /**
     * Create mock {@link HttpHeaders}.
     * @return The headers
     * @throws Exception If there is some problem inside
     */
    public HttpHeaders httpHeaders() throws Exception {
        return this.ihttpHeaders;
    }

    /**
     * Create mock {@link HttpServletRequest}.
     * @return The request
     * @throws Exception If there is some problem inside
     */
    public HttpServletRequest httpServletRequest() throws Exception {
        return this.ihttpRequest;
    }

    /**
     * Create mock {@link Providers}.
     * @return The providers
     * @throws Exception If there is some problem inside
     */
    public Providers providers() throws Exception {
        return this.iproviders;
    }

}
