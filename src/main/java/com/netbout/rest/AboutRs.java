/**
 * Copyright (c) 2009-2012, Netbout.com
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

import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.PageBuilder;
import com.rexsl.test.RestTester;
import com.rexsl.test.TestResponse;
import java.net.HttpURLConnection;
import java.net.URI;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * Friends finding service (used by RESTful client or AJAX).
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Path("/a")
public final class AboutRs extends BaseRs {

    /**
     * Get documentation page.
     * @param page Page name to show
     * @return The JAX-RS response
     * @todo #158 Path annotation: http://java.net/jira/browse/JERSEY-739
     */
    @GET
    @Path("/{page : .+}")
    public Response read(@PathParam("page") final String page) {
        final String html = new Markdown(this.markdown(page)).html();
        return new PageBuilder()
            .stylesheet("/xsl/about.xsl")
            .build(NbPage.class)
            .init(this)
            .append(new JaxbBundle("name", page))
            .append(new JaxbBundle("content", html))
            .preserved()
            .build();
    }

    /**
     * Fetch and return markdown content of the page.
     * @param page Page name to show
     * @return The markdown content of it
     * @todo #481 Would be great to cache these pages locally, or at least
     *  use If-Modified-Since HTTP header
     */
    private String markdown(final String page) {
        final URI uri = UriBuilder.fromUri("http://about.netbout.com/")
            .userInfo(String.format("a:%s", Manifests.read("Netbout-AboutPwd")))
            .path(String.format("%s.md", page))
            .build();
        final TestResponse response = RestTester.start(uri)
            .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML)
            .get(String.format("loading '%s' page", page));
        String markdown;
        if (response.getStatus() == HttpURLConnection.HTTP_OK) {
            markdown = response.getBody();
        } else {
            Logger.warn(
                this,
                "#read('%s'): '%s' returned #%d '%s'",
                page,
                uri,
                response.getStatus(),
                response.getBody()
            );
            markdown = String.format(
                "Page '%s' not found, try [index](/a/index).",
                page
            );
        }
        return markdown;
    }

}
