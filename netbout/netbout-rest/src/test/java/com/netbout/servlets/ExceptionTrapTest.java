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
package com.netbout.servlets;

import com.rexsl.test.XhtmlConverter;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.mockito.Mockito;
import org.xmlmatchers.XmlMatchers;
import org.xmlmatchers.namespace.SimpleNamespaceContext;

/**
 * Test case for {@link ExceptionTrap}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class ExceptionTrapTest {

    /**
     * ExceptionTrap can render page with exception.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void rendersExceptionIntoHtmlPage() throws Exception {
        final HttpServlet servlet = new ExceptionTrap();
        final HttpServletRequest request =
            Mockito.mock(HttpServletRequest.class);
        Mockito.doReturn("GET").when(request).getMethod();
        Mockito.doReturn("/").when(request).getRequestURI();
        Mockito.doReturn("a").when(request).getAttribute(Mockito.anyString());
        Mockito.doReturn(new java.io.IOException("ouch"))
            .when(request).getAttribute("javax.servlet.error.exception");
        final HttpServletResponse response =
            Mockito.mock(HttpServletResponse.class);
        final StringWriter writer = new StringWriter();
        Mockito.doReturn(new PrintWriter(writer)).when(response).getWriter();
        servlet.service(request, response);
        MatcherAssert.assertThat(
            XhtmlConverter.the(writer.toString()),
            XmlMatchers.hasXPath(
                "//xhtml:p[contains(.,'code: a')]",
                new SimpleNamespaceContext().withBinding(
                    "xhtml",
                    "http://www.w3.org/1999/xhtml"
                )
            )
        );
    }

    /**
     * ExceptionTrap can render page even if most of values are NULL.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void rendersHtmlPageWithNullAttributes() throws Exception {
        final HttpServlet servlet = new ExceptionTrap();
        final HttpServletRequest request =
            Mockito.mock(HttpServletRequest.class);
        Mockito.doReturn("POST").when(request).getMethod();
        Mockito.doReturn("/test").when(request).getRequestURI();
        final HttpServletResponse response =
            Mockito.mock(HttpServletResponse.class);
        final StringWriter writer = new StringWriter();
        Mockito.doReturn(new PrintWriter(writer)).when(response).getWriter();
        servlet.service(request, response);
    }

}
