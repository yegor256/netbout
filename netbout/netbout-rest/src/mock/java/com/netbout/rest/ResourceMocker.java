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

import com.netbout.bus.Bus;
import com.netbout.bus.BusMocker;
import com.netbout.hub.Hub;
import com.netbout.spi.Identity;
import com.netbout.spi.IdentityMocker;
import com.netbout.utils.Cryptor;
import com.rexsl.core.XslResolver;
import com.rexsl.test.XhtmlConverter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import javax.servlet.ServletContext;
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
 * Builds an instance of {@link Resource}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class ResourceMocker {

    /**
     * Bus.
     */
    private transient Bus bus = new BusMocker().mock();

    /**
     * Hub.
     */
    private transient Hub hub = Mockito.mock(Hub.class);

    /**
     * Message.
     */
    private transient String message;

    /**
     * Identity logged in.
     */
    private transient Identity identity = new IdentityMocker().mock();

    /**
     * Providers.
     */
    private transient Providers providers =
        Mockito.mock(Providers.class);

    /**
     * URI info.
     */
    private transient UriInfo uriInfo =
        Mockito.mock(UriInfo.class);

    /**
     * Http headers.
     */
    private transient HttpHeaders httpHeaders =
        Mockito.mock(HttpHeaders.class);

    /**
     * Servlet request.
     */
    private transient HttpServletRequest httpRequest =
        Mockito.mock(HttpServletRequest.class);

    /**
     * Public ctor.
     * @throws Exception If something is wrong
     */
    public ResourceMocker() throws Exception {
        final URI home = new URI("http://localhost:99/local");
        Mockito.doReturn(UriBuilder.fromUri(home))
            .when(this.uriInfo).getBaseUriBuilder();
        Mockito.doReturn(home)
            .when(this.uriInfo).getAbsolutePath();
        Mockito.doReturn(home)
            .when(this.uriInfo).getBaseUri();
        Mockito.doReturn(home.getHost()).when(this.httpRequest)
            .getRemoteAddr();
        Mockito.doReturn(home.getPath()).when(this.httpRequest)
            .getRequestURI();
        Mockito.doReturn(home.getPath()).when(this.httpRequest)
            .getContextPath();
        Mockito.doReturn(new XslResolver())
            .when(this.providers)
            .getContextResolver(
                Marshaller.class, MediaType.APPLICATION_XML_TYPE
            );
    }

    /**
     * With this message inside.
     * @param msg The message
     * @return This object
     */
    public ResourceMocker withMessage(final String msg) {
        this.message = msg;
        return this;
    }

    /**
     * With this identity.
     * @param idnt The identity
     * @return This object
     */
    public ResourceMocker withIdentity(final Identity idnt) {
        this.identity = idnt;
        return this;
    }

    /**
     * With this UriInfo.
     * @param info The object
     * @return This object
     */
    public ResourceMocker withUriInfo(final UriInfo info) {
        this.uriInfo = info;
        return this;
    }

    /**
     * With these dependencies.
     * @param ibus The bus
     * @param ihub The hub
     * @return This object
     */
    public ResourceMocker withDeps(final Bus ibus, final Hub ihub) {
        this.bus = ibus;
        this.hub = ihub;
        return this;
    }

    /**
     * Build an instance of provided class.
     * @param type The class to build
     * @param <T> The class of response
     * @return The resource just created
     * @throws Exception If something is wrong
     */
    public <T> T mock(final Class<? extends Resource> type) throws Exception {
        // @checkstyle IllegalType (1 line)
        final AbstractRs rest = (AbstractRs) type.newInstance();
        rest.setMessage(this.message);
        rest.setUriInfo(this.uriInfo);
        rest.setHttpHeaders(this.httpHeaders);
        rest.setHttpServletRequest(this.httpRequest);
        rest.setProviders(this.providers);
        rest.setCookie(new Cryptor().encrypt(this.identity));
        final ServletContext context = Mockito.mock(ServletContext.class);
        Mockito.doReturn(this.hub).when(context)
            .getAttribute("com.netbout.rest.HUB");
        Mockito.doReturn(this.bus).when(context)
            .getAttribute("com.netbout.rest.BUS");
        return (T) rest;
    }

    /**
     * Convert response to XML.
     * @param page The page
     * @param resource The resource, where this response came from
     * @return The XML
     * @throws Exception If there is some problem inside
     */
    public static Source the(final Page page, final Resource resource)
        throws Exception {
        final XslResolver resolver = (XslResolver) resource.providers()
            .getContextResolver(
                Marshaller.class,
                MediaType.APPLICATION_XML_TYPE
            );
        final Marshaller mrsh = resolver.getContext(page.getClass());
        mrsh.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        final StringWriter writer = new StringWriter();
        mrsh.marshal(page, writer);
        final Source source = XhtmlConverter.the(writer.toString());
        MatcherAssert.assertThat(
            source,
            XmlMatchers.hasXPath("/page[@nano]")
        );
        return source;
    }

}
