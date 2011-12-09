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
package com.netbout.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

/**
 * Text utils.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class TextUtils {

    /**
     * Encoding to be used.
     */
    private static final String ENCODING = "UTF-8";

    /**
     * It's utility class.
     */
    private TextUtils() {
        // empty
    }

    /**
     * URI encode.
     * @param uri The URI to encode
     * @return Encoded text
     */
    public static String ucode(final URI uri) {
        return StringEscapeUtils.escapeXml(uri.toString());
    }

    /**
     * Encode string into packed form.
     * @param text The text to encode
     * @return Encoded text
     */
    public static String pack(final String text) {
        assert text != null;
        try {
            return new Base64().encodeToString(
                text.getBytes(TextUtils.ENCODING)
            ).replaceAll("[\t\n\r]+", "");
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Decode string from packed form.
     * @param text The text to decode
     * @return Decoded text
     */
    public static String unpack(final String text) {
        assert text != null;
        try {
            return new String(new Base64().decode(text), TextUtils.ENCODING);
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Convert velocity context and template name into text.
     * @param name Name of template
     * @param context Velocity context
     * @return The text
     */
    public static String format(final String name,
        final VelocityContext context) {
        assert name != null;
        assert context != null;
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
        final Template template = engine.getTemplate(name);
        final StringWriter writer = new StringWriter();
        template.merge(context, new PrintWriter(writer));
        return writer.toString();
    }

}
