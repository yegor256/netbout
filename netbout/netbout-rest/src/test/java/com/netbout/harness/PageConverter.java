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

import com.netbout.rest.Page;
import com.netbout.rest.Resource;
import com.rexsl.core.XslResolver;
import com.rexsl.test.XhtmlConverter;
import java.io.StringWriter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Source;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.xmlmatchers.XmlMatchers;

/**
 * Converts response to XML.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class PageConverter {

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
            XmlMatchers.hasXPath("/page[@mcs]")
        );
        return source;
    }

}
