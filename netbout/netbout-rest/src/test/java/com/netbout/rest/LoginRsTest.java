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

import com.netbout.hub.Hub;
import com.netbout.hub.HubMocker;
import com.netbout.rest.auth.RemoteIdentity;
import com.netbout.spi.Identity;
import com.netbout.spi.IdentityMocker;
import com.netbout.spi.Urn;
import com.netbout.spi.UrnMocker;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import javax.ws.rs.core.Response;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.xmlmatchers.XmlMatchers;

/**
 * Test case for {@link LoginRs}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(LoginRs.class)
public final class LoginRsTest {

    /**
     * LoginRs can render a login page.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void rendersLoginPage() throws Exception {
        final LoginRs rest = new ResourceMocker().mock(LoginRs.class);
        rest.setAuth(Deee.plain("some-incorrect-auth-code"));
        final Response response = rest.login();
        MatcherAssert.assertThat(
            ResourceMocker.the((Page) response.getEntity(), rest),
            XmlMatchers.hasXPath("/page/links/link[@rel='facebook']")
        );
    }

    /**
     * LoginRs can authenticate through facebook.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void authenticateWithFacebook() throws Exception {
        final Urn name = new UrnMocker().withNid("netbout").mock();
        final Identity identity = new IdentityMocker().namedAs(name).mock();
        final Hub hub = new HubMocker()
            .withIdentity(name, identity)
            .doReturn(new ArrayList<String>(), "get-all-namespaces")
            .doReturn(new ArrayList<String>(), "get-aliases-of-identity")
            .mock();
        final LoginRs rest = new ResourceMocker()
            .withHub(hub)
            .mock(LoginRs.class);
        final LoginRs spy = PowerMockito.spy(rest);
        final RemoteIdentity remote = new RemoteIdentity();
        final URL photo = new URL("http://localhost/some-picture.png");
        final String alias = "some identity alias";
        remote.setAuthority("http://localhost/authority");
        remote.setName(name.toString());
        remote.setJaxbPhoto(photo.toString());
        remote.setAliases(Arrays.asList(new String[] {alias}));
        final String code = "some-auth-code";
        PowerMockito.doReturn(remote).when(spy, "remote", Mockito.eq(code));
        MatcherAssert.assertThat(
            ResourceMocker.the(
                (Page) spy.fbauth(Deee.plain(code)).getEntity(),
                rest
            ),
            Matchers.allOf(
                XmlMatchers.hasXPath(
                    String.format("/page/identity[name='%s']", name)
                ),
                XmlMatchers.hasXPath("/page/identity/aliases[count(alias) > 0]")
            )
        );
        Mockito.verify(identity).setPhoto(photo);
        Mockito.verify(identity).alias(alias);
    }

    /**
     * LoginRs can detect a situation when a logged in user is trying to login.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = ForwardException.class)
    public void forwardsIfUserAlreadyLoggedIn() throws Exception {
        ((LoginRs) new ResourceMocker().mock(LoginRs.class)).login();
    }

}
