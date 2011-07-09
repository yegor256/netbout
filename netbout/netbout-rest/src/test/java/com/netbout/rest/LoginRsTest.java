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
 * incident to the author by email: privacy@netbout.com.
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

import com.netbout.engine.User;
import com.netbout.engine.UserFactory;
import com.netbout.rest.jaxb.PageLogin;
import java.net.HttpCookie;
import java.util.List;
import javax.ws.rs.core.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @todo #107 This mechanism doesn't work at the moment, because it
 *       is not implemented.
 */
@Ignore
public final class LoginRsTest {

    private static final Long ID = 342L;

    private static final String PWD = "secret";

    /**
     * @link <a href="http://www.ietf.org/rfc/rfc2109.txt">RFC-2109</a>
     */
    private static final String SETCOOKIE_HEADER = "Set-Cookie";

    @Test
    public void testSignonProcess() throws Exception {
        final FactoryBuilder builder = mock(FactoryBuilder.class);
        final UserFactory factory = mock(UserFactory.class);
        final User user = mock(User.class);
        doReturn(factory).when(builder).getUserFactory();
        doReturn(user).when(factory).find(this.ID);
        doReturn(this.PWD).when(user).secret();
        final LoginRs svc = new LoginRs(builder);
        final String redirect = "/some-page.html";
        final Response response = svc.login(this.ID, this.PWD, redirect);
        verify(builder).getUserFactory();
        verify(factory).find(this.ID);
        verify(user).secret();
        assertThat(
            response.getStatus(),
            equalTo(HttpStatus.SC_TEMPORARY_REDIRECT)
        );
        assertThat(
            (String) response.getMetadata().getFirst(HttpHeaders.LOCATION),
            equalTo(redirect)
        );
        final List<HttpCookie> cookies = HttpCookie.parse(
            (String) response.getMetadata().getFirst(this.SETCOOKIE_HEADER)
        );
        HttpCookie found = null;
        for (HttpCookie cookie : cookies) {
            if (cookie.getName().equals(AbstractRs.COOKIE)) {
                found = cookie;
                break;
            }
        }
        assertThat(found, is(notNullValue()));
        final String authToken = found.getValue();
        assertThat(
            new Auth().decode(builder, authToken).number(),
            equalTo(this.ID)
        );
    }

    @Test
    public void testInvalidSignonProcess() throws Exception {
        final FactoryBuilder builder = mock(FactoryBuilder.class);
        final UserFactory factory = mock(UserFactory.class);
        final User user = mock(User.class);
        doReturn(factory).when(builder).getUserFactory();
        doReturn(user).when(factory).find(this.ID);
        doReturn("some-other-secret").when(user).secret();
        final LoginRs svc = new LoginRs(builder);
        final Response response = svc.login(this.ID, this.PWD, "/");
        verify(builder).getUserFactory();
        verify(factory).find(this.ID);
        verify(user).secret();
        assertThat(
            response.getStatus(),
            equalTo(HttpStatus.SC_FORBIDDEN)
        );
        final List<HttpCookie> cookies = HttpCookie.parse(
            (String) response.getMetadata().getFirst(this.SETCOOKIE_HEADER)
        );
        for (HttpCookie cookie : cookies) {
            assertThat(cookie.getName(), not(equalTo(AbstractRs.COOKIE)));
        }
    }

}
