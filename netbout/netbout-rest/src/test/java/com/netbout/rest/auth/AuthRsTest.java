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
package com.netbout.rest.auth;

import com.netbout.rest.ForwardException;
import com.netbout.rest.NbResourceMocker;
import com.netbout.spi.Identity;
import com.netbout.spi.IdentityMocker;
import com.netbout.spi.Urn;
import com.netbout.spi.UrnMocker;
import com.rexsl.test.ContainerMocker;
import java.net.HttpURLConnection;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

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
        final Urn iname = new UrnMocker().mock();
        final String photo = "http://localhost/some-pic.png";
        final ContainerMocker container = new ContainerMocker()
            .expectMethod(Matchers.equalTo("GET"))
            .expectHeader(
                HttpHeaders.ACCEPT,
                Matchers.containsString(MediaType.APPLICATION_XML)
            )
            .returnBody(
                // @checkstyle StringLiteralsConcatenation (7 lines)
                "<page><identity>"
                + "<aliases><alias>hello</alias></aliases>"
                + "<authority>http://localhost</authority>"
                + "<locale>zh_CN</locale>"
                + String.format("<name>%s</name>", iname)
                + String.format("<photo>%s</photo>", photo)
                + "</identity></page>"
            )
            .returnHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
            .mock();
        final String secret = "some secret";
        final Identity identity = new IdentityMocker()
            .namedAs(iname)
            .mock();
        final AuthRs rest = new NbResourceMocker()
            .withIdentity(identity)
            .withNamespaceURL(container.home().toURL())
            .mock(AuthRs.class);
        final Response response = rest.auth(iname, secret, "/some-path");
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
        final AuthRs rest = new NbResourceMocker()
            .withNamespaceURL(container.home().toURL())
            .mock(AuthRs.class);
        rest.auth(new Urn("foo", "test"), "", "/path-to-go");
    }

}
