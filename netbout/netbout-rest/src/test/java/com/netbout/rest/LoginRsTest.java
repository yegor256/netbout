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
package com.netbout.rest;

import com.rexsl.test.XhtmlMatchers;
import javax.ws.rs.core.Response;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * Test case for {@link LoginRs}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class LoginRsTest {

    /**
     * LoginRs can render a login page.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void rendersLoginPage() throws Exception {
        final LoginRs rest = new NbResourceMocker().mock(LoginRs.class);
        rest.setAuth("some-incorrect-auth-code");
        final Response response = rest.login();
        MatcherAssert.assertThat(
            NbResourceMocker.the((NbPage) response.getEntity(), rest),
            XhtmlMatchers.hasXPath("/page/links/link[@rel='facebook']")
        );
    }

    /**
     * LoginRs can detect a situation when a logged in user is trying to login,
     * and still allow him to see the login page.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void doesntForwardIfUserAlreadyLoggedIn() throws Exception {
        ((LoginRs) new NbResourceMocker().mock(LoginRs.class)).login();
    }

}
