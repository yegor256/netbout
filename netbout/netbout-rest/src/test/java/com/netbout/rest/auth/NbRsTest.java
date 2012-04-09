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

import com.netbout.rest.BasePage;
import com.netbout.rest.ForwardException;
import com.netbout.rest.ResourceMocker;
import com.netbout.spi.Urn;
import com.netbout.spi.text.SecureString;
import javax.ws.rs.core.Response;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xmlmatchers.XmlMatchers;

/**
 * Test case for {@link NbRs}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class NbRsTest {

    /**
     * NbRs can authenticate identity by user name, identity, and secret.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void authenticatesByNamesAndSecret() throws Exception {
        final Urn iname = new Urn("netbout", "hh");
        final NbRs rest = new ResourceMocker().mock(NbRs.class);
        final String secret = new SecureString(iname).toString();
        final Response response = rest.auth(iname, secret);
        MatcherAssert.assertThat(
            ResourceMocker.the((BasePage) response.getEntity(), rest),
            Matchers.allOf(
                XmlMatchers.hasXPath("//identity[alias='hh']"),
                XmlMatchers.hasXPath("//identity[name='urn:netbout:hh']"),
                XmlMatchers.hasXPath(
                    "//identity[authority='http://www.netbout.com/nb']"
                )
            )
        );
    }

    /**
     * NbRs can restrict access if secret code is wrong.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = ForwardException.class)
    public void doesntAuthenticateWithIncorrectSecret() throws Exception {
        final NbRs rest = new ResourceMocker().mock(NbRs.class);
        rest.auth(Urn.create("urn:foo:name"), "incorrect-secret-code");
    }

}
