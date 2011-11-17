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

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

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
    public void doGet(final HttpServletRequest request,
        final HttpServletResponse response) throws java.io.IOException {
        response.setContentType("text/html");
        final VelocityContext context = new VelocityContext();
        this.extend(context, request, "code");
        this.extend(context, request, "message");
        this.extend(context, request, "exception_type");
        this.extend(context, request, "request_uri");
        context.put(
            "stacktrace",
            this.stacktrace(
                (Throwable) request
                    .getAttribute("javax.servlet.error.exception")
            )
        );
        final Template template =
            this.engine().getTemplate("com/netbout/servlets/re.html.vm");
        final PrintWriter writer = response.getWriter();
        template.merge(context, writer);
        writer.close();
    }

    /**
     * Convert exception to string.
     * @param exp The exception
     * @return The stacktrace
     */
    private String stacktrace(final Throwable exp) {
        final StringWriter writer = new StringWriter();
        exp.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    /**
     * Extend velocity context with a value from java servlet.
     * @param context The context
     * @param request The request to get attributes from
     * @param suffix The suffix of java attribute
     */
    private void extend(final VelocityContext context,
        final HttpServletRequest request, final String suffix) {
        context.put(
            suffix,
            request.getAttribute(
                String.format("javax.servlet.error.%s", suffix)
            )
        );
    }

    /**
     * Get an instance of velocity.
     * @return The velocity
     */
    private VelocityEngine engine() {
        final VelocityEngine engine = new VelocityEngine();
        engine.setProperty("resource.loader", "cp");
        engine.setProperty(
            "cp.resource.loader.class",
            ClasspathResourceLoader.class.getName()
        );
        return engine;
    }

}
