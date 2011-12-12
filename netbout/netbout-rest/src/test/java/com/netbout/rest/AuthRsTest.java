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

import com.netbout.spi.Identity;
import com.netbout.spi.IdentityMocker;
import com.netbout.utils.Cipher;
import com.rexsl.test.ContainerMocker;
import java.net.HttpURLConnection;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xmlmatchers.XmlMatchers;

/**
 * Test case for {@link AuthRs}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class AuthRsTest {

    /**
     * AuthRs can authenticate identity by user name, identity, and secret.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void authenticatesByNamesAndSecret() throws Exception {
        final String iname = "nb:test";
        final String photo = "http://localhost/some-pic.png";
        final ContainerMocker container = new ContainerMocker()
            .expectMethod(Matchers.equalTo("GET"))
            .expectHeader(
                HttpHeaders.ACCEPT,
                Matchers.containsString(MediaType.APPLICATION_XML)
            )
            .returnBody(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "<page><identity><user>?</user><name>%s</name><photo>%s</photo></identity></page>",
                    iname,
                    photo
                )
            )
            .returnHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
            .mock();
        final String uname = UriBuilder
            .fromUri(container.home())
            .path("/")
            .build()
            .toString();
        final String secret = "some secret";
        final Identity identity = new IdentityMocker()
            .namedAs(iname)
            .belongsTo(uname)
            .mock();
        final AuthRs rest = new ResourceMocker()
            .withIdentity(identity)
            .mock(AuthRs.class);
        final Response response = rest.auth(uname, iname, secret);
        MatcherAssert.assertThat(
            response.getStatus(),
            Matchers.equalTo(HttpURLConnection.HTTP_SEE_OTHER)
        );
    }

    /**
     * AuthRs can properly report a problem if authentication fails.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = ForwardException.class)
    public void reportsProblemIfAuthenticationFails() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .returnStatus(HttpURLConnection.HTTP_NOT_FOUND)
            .mock();
        final String uname = UriBuilder
            .fromUri(container.home())
            .path("/")
            .build()
            .toString();
        final AuthRs rest = new ResourceMocker().mock(AuthRs.class);
        final Response response = rest.auth(uname, "", "");
    }

}
