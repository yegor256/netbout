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

import com.netbout.hub.Hub;
import com.netbout.hub.HubMocker;
import com.netbout.rest.NbPage;
import com.netbout.rest.NbResourceMocker;
import com.netbout.spi.Identity;
import com.netbout.spi.IdentityMocker;
import com.netbout.spi.Urn;
import com.netbout.spi.UrnMocker;
import com.rexsl.core.Manifests;
import com.rexsl.page.UriInfoMocker;
import com.rexsl.test.XhtmlMatchers;
import java.net.HttpURLConnection;
import java.net.URI;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link LoginRs}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class FacebookRsTest {

    /**
     * FacebookRs can authenticate without facebook, with super code.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void authenticatesWithSuperCode() throws Exception {
        final String fbid = "438947746483";
        // @checkstyle LineLength (1 line)
        final String code = String.format(
            "%s-%s",
            Manifests.read("Netbout-SuperSecret"),
            fbid
        );
        final FacebookRs rest = new NbResourceMocker().mock(FacebookRs.class);
        final Response response = rest.auth(Urn.create("urn:facebook:1"), code);
        MatcherAssert.assertThat(
            response.getStatus(),
            Matchers.equalTo(HttpURLConnection.HTTP_OK)
        );
        MatcherAssert.assertThat(
            NbResourceMocker.the((NbPage) response.getEntity(), rest),
            Matchers.allOf(
                XhtmlMatchers.hasXPath(
                    String.format("//identity[name='urn:facebook:%s']", fbid)
                ),
                XhtmlMatchers.hasXPath("//identity[locale='en']")
            )
        );
    }

}
