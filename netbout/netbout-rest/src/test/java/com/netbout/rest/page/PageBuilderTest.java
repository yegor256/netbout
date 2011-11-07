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
package com.netbout.rest.page;

import com.netbout.rest.AbstractPage;
import com.netbout.rest.Page;
import com.netbout.rest.Resource;
import com.rexsl.core.Stylesheet;
import com.rexsl.core.XslResolver;
import com.rexsl.test.JaxbConverter;
import java.net.URI;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.xmlmatchers.XmlMatchers;

/**
 * Test case for {@link PageBuilder}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class PageBuilderTest {

    /**
     * Object can be converted to XML through JAXB.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testJaxbIsWorking() throws Exception {
        final Resource resource = this.resource();
        final String stylesheet = "test-stylesheet";
        final Page page = new PageBuilder()
            .stylesheet(stylesheet)
            .build(AbstractPage.class)
            .init(resource);
        // double check duplicate instantiation
        new PageBuilder().stylesheet(stylesheet).build(AbstractPage.class);
        MatcherAssert.assertThat(
            page.getClass().getAnnotation(Stylesheet.class),
            Matchers.notNullValue()
        );
        MatcherAssert.assertThat(
            page.getClass().getAnnotation(Stylesheet.class).value(),
            Matchers.equalTo(stylesheet)
        );
        page.append(new PageBuilderTest.Foo());
        page.append(new PageBuilderTest.Foo());
        MatcherAssert.assertThat(
            JaxbConverter.the(page, PageBuilderTest.Foo.class),
            XmlMatchers.hasXPath("/page/foo/message[contains(.,'hello')]")
        );
    }

    /**
     * Create resource.
     * @return The resource, mocked
     * @throws Exception If there is some problem inside
     */
    private Resource resource() throws Exception {
        final Resource resource = Mockito.mock(Resource.class);
        final Providers providers = Mockito.mock(Providers.class);
        Mockito.doReturn(providers).when(resource).providers();
        final XslResolver resolver = new XslResolver();
        Mockito.doReturn(resolver).when(providers).getContextResolver(
            Marshaller.class,
            MediaType.APPLICATION_XML_TYPE
        );
        final UriInfo info = Mockito.mock(UriInfo.class);
        Mockito.doReturn(info).when(resource).uriInfo();
        final URI home = new URI("http://localhost/x");
        Mockito.doReturn(UriBuilder.fromUri(home))
            .when(info).getAbsolutePathBuilder();
        Mockito.doReturn(home).when(info).getAbsolutePath();
        return resource;
    }

    /**
     * Sub-class to add to Page as sub-element.
     */
    @XmlRootElement(name = "foo")
    @XmlAccessorType(XmlAccessType.NONE)
    public static final class Foo {
        /**
         * Get message.
         * @return The message
         */
        @XmlElement
        public String getMessage() {
            return "hello, world!";
        }
    }

}
