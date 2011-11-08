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
package com.netbout.rest;

import com.rexsl.core.Manifests;
import com.rexsl.test.JaxbConverter;
import java.net.URI;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.mockito.Mockito;
import org.xmlmatchers.XmlMatchers;

/**
 * Test case for {@link LoginRs}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ LoginRs.class })
public final class LoginRsTest {

    /**
     * Login page should be renderable.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testLoginPageRendering() throws Exception {
        final LoginRs rest = new LoginRs();
        rest.setUriInfo(this.uriInfo(new URI("http://localhost:99/")));
        final Page page = rest.login();
        MatcherAssert.assertThat(
            JaxbConverter.the(page),
            XmlMatchers.hasXPath("/page/facebook[@href]")
        );
        MatcherAssert.assertThat(
            JaxbConverter.the(page),
            XmlMatchers.hasXPath("/page/version/name[.='1.0']")
        );
        MatcherAssert.assertThat(
            JaxbConverter.the(page),
            XmlMatchers.hasXPath("/page/links/link[@name='self']")
        );
    }

    /**
     * Facebook callback should produce a cookie.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testAuthenticationFromFacebook() throws Exception {
        final String code = "123";
        final LoginRs rest = new LoginRs();
        final URI home = new URI("http://localhost/");
        rest.setUriInfo(this.uriInfo(home));
        final LoginRs spy = PowerMockito.spy(rest);
        final String token = "access_token=abc|cde&expires=5108";
        PowerMockito.doReturn(token).when(
            spy,
            "retrieve",
            Mockito.eq(
                UriBuilder
                    .fromPath("https://graph.facebook.com/oauth/access_token")
                    .queryParam("client_id", Manifests.read("Netbout-FbId"))
                    .queryParam("redirect_uri", home.toString())
                    .queryParam(
                        "client_secret",
                        Manifests.read("Netbout-FbSecret")
                    )
                    .queryParam("code", code)
                    .build()
            )
        );
        PowerMockito.doReturn("{ name: 'John Doe' }").when(
            spy,
            "retrieve",
            Mockito.eq(
                UriBuilder
                    .fromPath("https://graph.facebook.com/me")
                    .replaceQuery(token)
                    .build()
            )
        );
        final Response response = spy.fbauth("123");
        MatcherAssert.assertThat(response, Matchers.notNullValue());
    }

    /**
     * Create mock UriInfo.
     * @param home Home to return
     * @throws Exception If there is some problem inside
     */
    private UriInfo uriInfo(final URI home) throws Exception {
        final UriInfo info = Mockito.mock(UriInfo.class);
        Mockito.doReturn(UriBuilder.fromUri(home))
            .when(info).getAbsolutePathBuilder();
        Mockito.doReturn(home).when(info).getAbsolutePath();
        return info;
    }

}
