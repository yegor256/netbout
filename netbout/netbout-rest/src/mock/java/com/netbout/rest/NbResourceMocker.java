/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code accidentally and without intent to use it, please report this
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
import com.netbout.hub.HubMocker;
import com.netbout.hub.UrnResolver;
import com.netbout.spi.Identity;
import com.netbout.spi.IdentityMocker;
import com.netbout.spi.Urn;
import com.rexsl.core.XslResolver;
import com.rexsl.page.Resource;
import com.rexsl.page.ResourceMocker;
import com.rexsl.test.XhtmlMatchers;
import java.io.StringWriter;
import java.net.URL;
import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.Marshaller;
import org.hamcrest.MatcherAssert;
import org.mockito.Mockito;

/**
 * Builds an instance of {@link NbResource}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class NbResourceMocker {

    /**
     * Hub.
     */
    private transient Hub hub;

    /**
     * Message.
     */
    private transient String message;

    /**
     * Identity logged in.
     */
    private transient Identity identity = new IdentityMocker().mock();

    /**
     * URL for all namespaces.
     */
    private transient URL namespaceUrl;

    /**
     * Providers.
     */
    private final transient ResourceMocker resource = new ResourceMocker();

    /**
     * With this URL for all namespaces.
     * @param url The URL
     * @return This object
     */
    public NbResourceMocker withNamespaceURL(final URL url) {
        this.namespaceUrl = url;
        return this;
    }

    /**
     * With this message inside.
     * @param msg The message
     * @return This object
     */
    public NbResourceMocker withMessage(final String msg) {
        this.message = msg;
        return this;
    }

    /**
     * With this identity.
     * @param idnt The identity
     * @return This object
     */
    public NbResourceMocker withIdentity(final Identity idnt) {
        this.identity = idnt;
        return this;
    }

    /**
     * With this UriInfo.
     * @param info The object
     * @return This object
     */
    public NbResourceMocker withUriInfo(final UriInfo info) {
        this.resource.withUriInfo(info);
        return this;
    }

    /**
     * With this hub.
     * @param ihub The hub
     * @return This object
     */
    public NbResourceMocker withHub(final Hub ihub) {
        this.hub = ihub;
        return this;
    }

    /**
     * Build an instance of provided class.
     * @param type The class to build
     * @param <T> The class of response
     * @return The resource just created
     * @throws Exception If any
     */
    public <T extends Resource> T mock(final Class<T> type) throws Exception {
        if (this.hub == null) {
            final Urn iname = this.identity.name();
            this.hub = new HubMocker()
                .withIdentity(iname, this.identity)
                .mock();
            if (this.namespaceUrl != null) {
                final UrnResolver resolver = Mockito.mock(UrnResolver.class);
                Mockito.doReturn(this.namespaceUrl).when(resolver)
                    .authority(Mockito.any(Urn.class));
                Mockito.doReturn(resolver).when(this.hub).resolver();
            }
        }
        // @checkstyle IllegalType (1 line)
        final BaseRs rest = (BaseRs) type.newInstance();
        rest.setMessage(this.message);
        final Resource res = this.resource.mock();
        rest.setUriInfo(res.uriInfo());
        rest.setHttpHeaders(res.httpHeaders());
        rest.setHttpServletRequest(res.httpServletRequest());
        rest.setProviders(res.providers());
        rest.setCookie(new CryptedIdentity(this.identity).toString());
        final ServletContext context = Mockito.mock(ServletContext.class);
        Mockito.doReturn(this.hub).when(context)
            .getAttribute("com.netbout.rest.HUB");
        rest.setServletContext(context);
        return type.cast(rest);
    }

    /**
     * Convert response to XML.
     * @param page The page
     * @param resource The resource, where this response came from
     * @return The XML
     * @throws Exception If any
     */
    public static String the(final NbPage page, final NbResource resource)
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
        final String xml = writer.toString();
        MatcherAssert.assertThat(xml, XhtmlMatchers.hasXPath("/page/millis"));
        return xml;
    }

}
