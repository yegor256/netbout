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
package com.netbout.spi.client;

import com.netbout.spi.Identity;
import com.rexsl.test.RestTester;
import com.rexsl.test.TestResponse;
import com.ymock.util.Logger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

/**
 * Expert of REST features.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class RestExpert {

    /**
     * Entry point.
     */
    private final transient UriBuilder home;

    /**
     * Public ctor.
     * @param identity The identity
     */
    public RestExpert(final Identity identity) {
        this.home = RestUriBuilder.from(identity);
    }

    /**
     * Promote me with this URL.
     * @param url The URL of helper
     */
    public void promote(final URL url) {
        final TestResponse entry = RestTester.start(this.home)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
            .get("home page")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .rel("/page/links/link[@rel='helper']/@href")
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
            .get("reading promotion page")
            .assertStatus(HttpURLConnection.HTTP_OK);
        if (entry.xpath("/page/identity/@helper").isEmpty()) {
            entry
                .rel("/page/links/link[@rel='promote']/@href")
                .post(
                    "promoting helper",
                    String.format("url=%s", URLEncoder.encode(url.toString()))
                )
                .assertStatus(HttpURLConnection.HTTP_SEE_OTHER);
        } else {
            final String location = entry
                .xpath("/page/identity/location/text()")
                .get(0);
            if (location.equals(url.toString())) {
                Logger.warn(
                    this,
                    "#promote('%s'): already promoted, won't do it again",
                    url
                );
            } else {
                throw new IllegalStateException(
                    String.format(
                        "You're already a helper with '%s', can't promote",
                        location
                    )
                );
            }
        }
    }

    /**
     * Get namespaces of the person.
     * @return Active map of thems
     */
    public Map<String, URL> namespaces() {
        return new RestNamespaces(this.home);
    }

}
