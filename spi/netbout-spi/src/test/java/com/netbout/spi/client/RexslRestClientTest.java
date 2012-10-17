/**
 * Copyright (c) 2009-2012, Netbout.com
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

import com.rexsl.test.ContainerMocker;
import com.rexsl.test.RestTester;
import com.rexsl.test.TestClient;
import com.rexsl.test.TestClientMocker;
import java.net.HttpURLConnection;
import java.net.URI;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link RexslRestClient}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class RexslRestClientTest {

    /**
     * RexslRestClient can return URI of home page.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void returnsUri() throws Exception {
        final String uri = "http://localhost/some";
        final String token = "some secret token";
        final URI target = UriBuilder
            .fromUri(uri)
            .queryParam(RestSession.AUTH_PARAM, token)
            .build();
        final TestClient tclient = new TestClientMocker()
            .withUri(uri)
            .mock();
        final RestClient client = new RexslRestClient(tclient, token);
        MatcherAssert.assertThat(
            client.uri(),
            Matchers.equalTo(target)
        );
    }

    /**
     * RexslRestClient can return URI of home page.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void sendsGetRequestWithParams() throws Exception {
        final String token = "some auth token";
        final String cookie = new Cookie(RestSession.AUTH_COOKIE, token)
            .toString();
        final ContainerMocker container = new ContainerMocker()
            .expectRequestUri(Matchers.endsWith("foo"))
            .expectMethod(Matchers.equalTo(RestTester.GET))
            .expectHeader(HttpHeaders.COOKIE, Matchers.equalTo(cookie))
            .expectHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
            .returnBody("<page><identity><eta>0</eta></identity><a/></page>")
            .returnStatus(HttpURLConnection.HTTP_OK)
            .mock();
        final URI uri = UriBuilder.fromUri(container.home())
            .path("/foo")
            .build();
        new RexslRestClient(RestTester.start(uri), token)
            .get("just a test")
            .assertXPath("/page/a")
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * RexslRestClient can properly encode URI.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void encodesUriWithAllParamsProperly() throws Exception {
        final URI uri = new RexslRestClient(
            new TestClientMocker().mock(), "token"
        ).queryParam("p1", "100% %40").uri();
        MatcherAssert.assertThat(
            uri.toString(),
            Matchers.equalTo("http://localhost/?p1=100%25+%2540&auth=token")
        );
    }

}
