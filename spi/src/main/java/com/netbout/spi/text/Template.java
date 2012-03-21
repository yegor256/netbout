/**
 * Copyright (c) 2009-2011, NetBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi.text;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

/**
 * Template to be filled with values.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class Template {

    /**
     * Name of resource.
     */
    private final transient String name;

    /**
     * The context.
     */
    private final transient VelocityContext context = new VelocityContext();

    /**
     * Public ctor.
     * @param res Name of resource with template
     */
    public Template(final String res) {
        assert res != null;
        this.name = res;
    }

    /**
     * Add new value.
     * @param prop Name of the property to set
     * @param value The value
     * @return This object
     */
    public Template set(final String prop, final Object value) {
        this.context.put(prop, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final VelocityEngine engine = new VelocityEngine();
        engine.setProperty("resource.loader", "cp");
        engine.setProperty(
            "cp.resource.loader.class",
            ClasspathResourceLoader.class.getName()
        );
        engine.setProperty(
            RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
            "org.apache.velocity.runtime.log.Log4JLogChute"
        );
        engine.setProperty(
            "runtime.log.logsystem.log4j.logger",
            "org.apache.velocity"
        );
        engine.init();
        final org.apache.velocity.Template template =
            engine.getTemplate(this.name);
        final StringWriter writer = new StringWriter();
        template.merge(this.context, new PrintWriter(writer));
        return writer.toString();
    }

}
