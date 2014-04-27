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
package com.netbout.rest;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import java.net.HttpURLConnection;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.junit.Test;

/**
 * Integration case for {@link BaseRs}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class BaseRsITCase {

    /**
     * Home page of Tomcat.
     */
    private static final String HOME = System.getProperty("tomcat.home");

    /**
     * BaseRs can render static resources.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void downloadsStaticResources() throws Exception {
        final String[] pages = {
            "/robots.txt",
            "/css/global.css",
            "/js/bout.js",
            "/xsl/login.xsl",
            "/lang/en.xml",
        };
        for (final String page : pages) {
            new JdkRequest(BaseRsITCase.HOME)
                .uri().path(page).back()
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK);
        }
    }

    /**
     * BaseRs can render non-found pages.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void downloadsNonFoundResources() throws Exception {
        final String[] pages = {
            "/the-page-doesnt-exist",
            "/-this-one-also",
        };
        for (final String page : pages) {
            new JdkRequest(BaseRsITCase.HOME)
                .uri().path(page).back()
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_NOT_FOUND);
        }
    }

}
