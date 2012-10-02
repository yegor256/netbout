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

import com.netbout.spi.IdentityMocker;
import com.rexsl.page.HttpHeadersMocker;
import com.rexsl.page.UriInfoMocker;
import java.net.URI;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test case for {@link BaseRs}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id: BoutRsTest.java 2482 2012-05-22 10:18:34Z guard $
 */
public final class BaseRsTest {

    /**
     * BaseRs can build base URI.
     * @throws Exception If there is some problem inside
     * @see http://trac.fazend.com/rexsl/ticket/570
     */
    @Test
    public void buildsBaseUri() throws Exception {
        final URI uri = new URI("http://test.netbout.com:324/");
        final UriInfo info = new UriInfoMocker()
            .withBaseUri(uri)
            .mock();
        final BaseRs rest = new NbResourceMocker()
            .withUriInfo(info)
            .mock(BaseRs.class);
        MatcherAssert.assertThat(
            rest.base().build(),
            Matchers.equalTo(uri)
        );
    }

    /**
     * BaseRs can forward to HTTPS, when necessary.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void forwardsToHttps() throws Exception {
        final URI uri = new URI("HTTP://test.netbout.com:32435/FOO");
        final UriInfo info = new UriInfoMocker()
            .withBaseUri(uri)
            .withRequestUri(uri)
            .mock();
        final BaseRs rest = new NbResourceMocker()
            .withUriInfo(info)
            .mock(BaseRs.class);
        rest.setCookie(
            new CryptedIdentity(new IdentityMocker().mock()).toString()
        );
        try {
            rest.identity();
            Assert.fail("redirection expected");
        } catch (javax.ws.rs.WebApplicationException ex) {
            MatcherAssert.assertThat(
                ex.getResponse(),
                Matchers.allOf(
                    Matchers.hasProperty(
                        "status",
                        Matchers.equalTo(
                            Response.Status.TEMPORARY_REDIRECT.getStatusCode()
                        )
                    ),
                    Matchers.hasProperty(
                        "metadata",
                        Matchers.hasEntry(
                            Matchers.equalTo(HttpHeaders.LOCATION),
                            Matchers.hasItem(
                                // @checkstyle MultipleStringLiterals (1 line)
                                UriBuilder.fromUri(uri).scheme("https").build()
                            )
                        )
                    )
                )
            );
        }
    }

    /**
     * BaseRs can avoid forwarding to HTTPS, when not required.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void doesntForwardToHttps() throws Exception {
        final URI uri = new URI("HTTPS://test.netbout.com:32435/bar");
        final UriInfo info = new UriInfoMocker()
            .withBaseUri(uri)
            .withRequestUri(uri)
            .mock();
        final BaseRs rest = new NbResourceMocker()
            .withUriInfo(info)
            .mock(BaseRs.class);
        rest.setCookie(
            new CryptedIdentity(new IdentityMocker().mock()).toString()
        );
        rest.identity();
    }

    /**
     * BaseRs can avoid forwarding to HTTPS, when Heroku forward is there.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void doesntForwardToHttpsInHeroku() throws Exception {
        final URI uri = new URI("http://test.netbout.com:80/heroku");
        final UriInfo info = new UriInfoMocker()
            .withBaseUri(uri)
            .withRequestUri(uri)
            .mock();
        final HttpHeaders headers = new HttpHeadersMocker()
            .withHeader("x-forwarded-proto", "https")
            .mock();
        final BaseRs rest = new NbResourceMocker()
            .withUriInfo(info)
            .withHttpHeaders(headers)
            .mock(BaseRs.class);
        rest.setCookie(
            new CryptedIdentity(new IdentityMocker().mock()).toString()
        );
        rest.identity();
    }

}
