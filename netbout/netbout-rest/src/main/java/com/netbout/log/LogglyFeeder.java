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
package com.netbout.log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.io.IOUtils;

/**
 * Log appender, for over-HTTP events.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class LogglyFeeder implements Feeder {

    /**
     * The access key.
     */
    private transient String key;

    /**
     * Set option {@code key}.
     * @param name The key
     */
    public void setKey(final String name) {
        this.key = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void feed(final String text) throws IOException {
        URL url;
        try {
            url = UriBuilder.fromUri("https://logs.loggly.com/inputs/")
                .path("/{key}")
                .build(this.key)
                .toURL();
        } catch (java.net.MalformedURLException ex) {
            throw new IOException(ex);
        }
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        try {
            conn.setRequestMethod("POST");
        } catch (java.net.ProtocolException ex) {
            throw new IOException(ex);
        }
        conn.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
        IOUtils.write(text, conn.getOutputStream());
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException(
                String.format(
                    "Invalid response code #%d from %s",
                    conn.getResponseCode(),
                    url
                )
            );
        }
    }

}
