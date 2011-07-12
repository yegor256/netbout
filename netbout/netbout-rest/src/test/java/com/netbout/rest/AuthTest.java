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
import java.util.HashSet;
import java.util.Set;
import org.junit.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @todo #107 This test doesn't work because authentication functionality
 *       is not implemented yet. We should implement it, using some
 *       simple encryption/decryption algorithm with salt.
 */
@Ignore
public final class AuthTest {

    private static final Long ID = 12773L;

    private static final String PWD = "some-secret-123";

    private static final Integer STRENGTH_CYCLES = 20;

    @Test
    public void testEncodingAndDecodingMechanism() throws Exception {
        final FactoryBuilder builder = mock(FactoryBuilder.class);
        final UserFactory factory = mock(UserFactory.class);
        doReturn(factory).when(builder).getUserFactory();
        final User user = mock(User.class);
        doReturn(this.ID).when(user).number();
        doReturn(this.PWD).when(user).secret();
        doReturn(user).when(factory).find(this.ID);

        final Auth auth = new Auth();
        final String token = auth.encode(user);
        verify(user).secret();
        assertThat(auth.decode(builder, token).number(), equalTo(this.ID));
        verify(builder).getUserFactory();
        verify(factory).find(this.ID);
        verify(user).secret();
    }

    @Test(expected = NotLoggedInException.class)
    public void testInvalidPassword() throws Exception {
        // build token with invalid password
        final User invalid = mock(User.class);
        doReturn(this.ID).when(invalid).number();
        doReturn("invalid-password").when(invalid).secret();
        final String token = new Auth().encode(invalid);
        // let's try to login with this token
        final FactoryBuilder builder = mock(FactoryBuilder.class);
        final UserFactory factory = mock(UserFactory.class);
        doReturn(factory).when(builder).getUserFactory();
        final User user = mock(User.class);
        doReturn(this.ID).when(user).number();
        doReturn(this.PWD).when(user).secret();
        doReturn(user).when(factory).find(this.ID);
        new Auth().decode(builder, token);
    }

    @Test(expected = NotLoggedInException.class)
    public void testDecodeFromInvalidToken() throws Exception {
        new Auth().decode(
            mock(FactoryBuilder.class),
            "some-invalid-auth-token"
        );
    }

    @Test(expected = NotLoggedInException.class)
    public void testDecodeFromEmptyToken() throws Exception {
        new Auth().decode(mock(FactoryBuilder.class), "");
    }

    @Test
    public void testStrengthOfEncodingAndDecodingMechanism() throws Exception {
        final FactoryBuilder builder = mock(FactoryBuilder.class);
        final UserFactory factory = mock(UserFactory.class);
        doReturn(factory).when(builder).getUserFactory();
        final User user = mock(User.class);
        doReturn(this.ID).when(user).number();
        doReturn(this.PWD).when(user).secret();
        doReturn(user).when(factory).find(this.ID);

        final Auth auth = new Auth();
        final Set<String> tokens = new HashSet<String>();
        // every time we get a token, it has to be unique, even if
        // the user is the same
        for (int num = 0; num < this.STRENGTH_CYCLES; num += 1) {
            final String token = auth.encode(user);
            assertThat(tokens, not(hasItem(token)));
            tokens.add(token);
        }
    }

}
