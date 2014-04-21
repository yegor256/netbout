/**
 * Copyright (c) 2009-2014, Netbout.com
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
package com.netbout.rest.jet;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Http jet.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
final class HttpJet implements Jet {

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    public Response build(final URI uri) throws IOException {
        final HttpURLConnection conn = (HttpURLConnection)
            uri.toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.connect();
        final Response.ResponseBuilder builder = Response.ok(new Output(conn));
        final Map<String, List<String>> headers = conn.getHeaderFields();
        for (String name : headers.keySet()) {
            if (!StringUtils.equalsIgnoreCase(name, HttpHeaders.CONTENT_TYPE)
                && !StringUtils
                    .equalsIgnoreCase(name, HttpHeaders.CONTENT_LENGTH)) {
                continue;
            }
            for (String value : headers.get(name)) {
                builder.header(name, value);
            }
        }
        return builder.build();
    }

    /**
     * The content streamer.
     */
    private final class Output implements StreamingOutput {
        /**
         * The connection to use.
         */
        private final transient HttpURLConnection connection;
        /**
         * Public ctor.
         * @param conn The connection
         */
        public Output(final HttpURLConnection conn) {
            this.connection = conn;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void write(final OutputStream stream) throws IOException {
            try {
                IOUtils.copy(this.connection.getInputStream(), stream);
            } finally {
                this.connection.disconnect();
            }
        }
    }

}
