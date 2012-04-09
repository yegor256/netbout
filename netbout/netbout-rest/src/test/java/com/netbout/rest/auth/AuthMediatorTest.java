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

import com.netbout.hub.UrnResolver;
import com.netbout.hub.UrnResolverMocker;
import com.netbout.spi.Urn;
import com.rexsl.test.ContainerMocker;
import java.util.Locale;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link AuthMediator}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class AuthMediatorTest {

    /**
     * AuthMediator can load facebook identity.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void authenticatesFacebookIdentity() throws Exception {
        final Urn iname = new Urn(FacebookRs.NAMESPACE, "1234567");
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
                + "<locale>zh</locale>"
                + String.format("<name>%s</name>", iname)
                + String.format("<photo>%s</photo>", photo)
                + "</identity></page>"
            )
            .returnHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
            .mock();
        final UrnResolver resolver = new UrnResolverMocker()
            .resolveAs(FacebookRs.NAMESPACE, container.home().toURL())
            .mock();
        final RemoteIdentity identity = new AuthMediator(resolver)
            .authenticate(new Urn(FacebookRs.NAMESPACE, ""), "secret-1");
        MatcherAssert.assertThat(identity.name(), Matchers.equalTo(iname));
        MatcherAssert.assertThat(
            identity.profile().photo().toString(),
            Matchers.equalTo(photo)
        );
        MatcherAssert.assertThat(
            identity.profile().locale(),
            Matchers.equalTo(Locale.CHINESE)
        );
        MatcherAssert.assertThat(
            identity.profile().aliases().size(),
            Matchers.equalTo(1)
        );
    }

    /**
     * AuthMediator can handle broken input.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = java.io.IOException.class)
    public void throwsExceptionWithBrokenInput() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .returnBody("<page></page>")
            .returnHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
            .mock();
        final UrnResolver resolver = new UrnResolverMocker()
            .resolveAs(FacebookRs.NAMESPACE, container.home().toURL())
            .mock();
        final RemoteIdentity identity = new AuthMediator(resolver)
            .authenticate(new Urn(FacebookRs.NAMESPACE, ""), "secret-2");
        identity.name();
    }

    /**
     * AuthMediator can work with missed data.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void authenticatesWithMissedData() throws Exception {
        final ContainerMocker container = new ContainerMocker()
            .returnBody(
                // @checkstyle StringLiteralsConcatenation (4 lines)
                "<page><identity> "
                + "<authority>http://localhost</authority> "
                + "<name>urn:test:abc</name> "
                + " </identity></page>"
        )
            .returnHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
            .mock();
        final UrnResolver resolver = new UrnResolverMocker()
            .resolveAs(FacebookRs.NAMESPACE, container.home().toURL())
            .mock();
        final RemoteIdentity identity = new AuthMediator(resolver)
            .authenticate(new Urn(FacebookRs.NAMESPACE, ""), "secret-5");
        MatcherAssert.assertThat(
            identity.profile().photo(),
            Matchers.notNullValue()
        );
        MatcherAssert.assertThat(
            identity.profile().locale(),
            Matchers.equalTo(Locale.ENGLISH)
        );
        MatcherAssert.assertThat(
            identity.profile().aliases().size(),
            Matchers.equalTo(0)
        );
    }

}
