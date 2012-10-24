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

import com.rexsl.page.JaxbBundle;
import com.rexsl.page.PageBuilder;
import com.rexsl.test.RestTester;
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
 * @version $Id: FriendsRs.java 3465 2012-10-16 18:31:35Z guard $
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
    @Path("/{page}")
    public Response read(@PathParam("page") final String page) {
        final URI uri = UriBuilder.fromUri("http://about.netbout.com/")
            .path(String.format("%s.md", page))
            .build();
        final String markdown = RestTester.start(uri)
            .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML)
            .get(String.format("loading '%s' page", page))
            .assertStatus(HttpURLConnection.HTTP_OK)
            .getBody();
        final String html = new Markdown(markdown).html();
        return new PageBuilder()
            .stylesheet("/xsl/about.xsl")
            .build(NbPage.class)
            .init(this)
            .append(new JaxbBundle("name", page))
            .append(new JaxbBundle("content", html))
            .render()
            .preserved()
            .build();
    }

}
