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
package com.netbout.rest.auth;

import com.netbout.hub.Hub;
import com.netbout.hub.HubMocker;
import com.netbout.rest.Deee;
import com.netbout.rest.ResourceMocker;
import com.netbout.rest.UriInfoMocker;
import com.netbout.spi.Identity;
import com.netbout.spi.IdentityMocker;
import com.netbout.spi.Urn;
import com.netbout.spi.UrnMocker;
import com.rexsl.core.Manifests;
import java.net.HttpURLConnection;
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

/**
 * Test case for {@link LoginRs}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(FacebookRs.class)
public final class FacebookRsTest {

    /**
     * LoginRs can authenticate user through Facebook.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void authenticatesUserThroughFacebook() throws Exception {
        // @checkstyle LineLength (1 line)
        final String code = "AQCJ9EpLpqvj9cbag0mU8z6cHqyk-2CN5cigCzwB1aykqqqpiFNzAjsnNbRRY7x4n4h2ZEmrRVHhHSHzcFTtXobWM8LJSCHSB1_cjvsJS2vy2DsqRA3qGRAjUY8pKk0tO2zYpX-kFpnn2V6Z1xxvb7uyP-qrV_mQNWSYHKfPWKL0yTxo-NpFAGT4mDYNXl_cCMs";
        final URI base = new URI("http://localhost/test/me");
        final String fbid = "438947328947329";
        final Urn iname = new UrnMocker()
            .withNid(FacebookRs.NAMESPACE)
            .mock();
        final Identity identity = new IdentityMocker().namedAs(iname).mock();
        final Hub hub = new HubMocker()
            .withIdentity(iname, identity)
            .mock();
        final UriInfo info = new UriInfoMocker()
            .withRequestUri(base)
            .mock();
        final URI redirect = UriBuilder.fromUri(base).path("/g/fb").build();
        final FacebookRs rest = new ResourceMocker()
            .withHub(hub)
            .withUriInfo(info)
            .mock(FacebookRs.class);
        final FacebookRs spy = PowerMockito.spy(rest);
        PowerMockito.doReturn("access_token=abc|cde&expires=5108").when(
            spy,
            // @checkstyle MultipleStringLiterals (1 line)
            "retrieve",
            Mockito.eq(
                UriBuilder
                    .fromPath("https://graph.facebook.com/oauth/access_token")
                    .queryParam("client_id", Manifests.read("Netbout-FbId"))
                    .queryParam("redirect_uri", redirect)
                    .queryParam(
                        "client_secret",
                        Manifests.read("Netbout-FbSecret")
                    )
                    .queryParam("code", code)
                    .build()
            )
        );
        final com.restfb.types.User fbuser =
            Mockito.mock(com.restfb.types.User.class);
        Mockito.doReturn(fbid).when(fbuser).getId();
        Mockito.doReturn("John Doe").when(fbuser).getName();
        PowerMockito.doReturn(fbuser).when(spy, "fbUser", "abc|cde");
        final Response response = spy.auth(Deee.plain(iname), Deee.plain(code));
        MatcherAssert.assertThat(
            response.getStatus(),
            Matchers.equalTo(HttpURLConnection.HTTP_OK)
        );
    }

}
