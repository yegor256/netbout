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
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;

/**
 * Feeder through HTTP POST request.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class HttpFeeder implements Feeder {

    /**
     * The URL to post to.
     */
    private transient URL url;

    /**
     * Set option {@code url}.
     * @param addr The URL
     */
    public void setUrl(final String addr) {
        try {
            this.url = new URL(addr);
        } catch (java.net.MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void feed(final String text) throws IOException {
        for (String line : text.split(CloudAppender.EOL)) {
            this.post(String.format("%s%s", line, CloudAppender.EOL));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activateOptions() {
        // empty
    }

    /**
     * POST one line of text.
     * @param text The text to post
     * @throws IOException If failed to post
     */
    private void post(final String text) throws IOException {
        final HttpURLConnection conn =
            (HttpURLConnection) this.url.openConnection();
        conn.setConnectTimeout((int) TimeUnit.MINUTES.toMillis(1L));
        conn.setReadTimeout((int) TimeUnit.MINUTES.toMillis(1L));
        conn.setDoOutput(true);
        try {
            conn.setRequestMethod("POST");
        } catch (java.net.ProtocolException ex) {
            throw new IOException(ex);
        }
        conn.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
        IOUtils.write(
            text,
            conn.getOutputStream(),
            CharEncoding.UTF_8
        );
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException(
                String.format(
                    "Invalid response code #%d from %s",
                    conn.getResponseCode(),
                    this.url
                )
            );
        }
    }

}
