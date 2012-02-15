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

import com.netbout.text.Template;
import com.ymock.util.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * All uncaught exceptions will be catched here.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class ExceptionTrap extends HttpServlet {

    /**
     * {@inheritDoc}
     */
    @Override
    public void service(final HttpServletRequest request,
        final HttpServletResponse response) throws java.io.IOException {
        response.setContentType("text/html");
        final Template template =
            new Template("com/netbout/servlets/re.html.vm");
        this.extend(template, request, "code");
        this.extend(template, request, "message");
        this.extend(template, request, "exception_type");
        this.extend(template, request, "request_uri");
        template.set(
            "stacktrace",
            StringEscapeUtils.escapeHtml(
                Logger.format(
                    "%[exception]s",
                    request.getAttribute("javax.servlet.error.exception")
                )
            )
        );
        response.getWriter().print(template.toString());
        response.getWriter().close();
    }

    /**
     * Extend velocity context with a value from java servlet.
     * @param template The template to extend
     * @param request The request to get attributes from
     * @param suffix The suffix of java attribute
     */
    private void extend(final Template template,
        final HttpServletRequest request, final String suffix) {
        Object attr = request.getAttribute(
            String.format("javax.servlet.error.%s", suffix)
        );
        if (attr == null) {
            attr = "NULL";
        }
        template.set(suffix, StringEscapeUtils.escapeHtml(attr.toString()));
    }

}
