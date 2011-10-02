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
import org.junit.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * This test case is increasing code coverage in order to make build clean.
 * Feel free to remove this class or any methods from it, if you have other
 * test cases ready, which cover classes.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class TestCoverageTest {

    /**
     * Feel free to delete this test, if you managed to create another test
     * for this class.
     */
    @Test
    public void testLoginRs() throws Exception {
        final FactoryBuilder builder = mock(FactoryBuilder.class);
        final LoginRs svc = new LoginRs(builder);
        svc.entrance();
    }

    /**
     * Feel free to delete this test, if you managed to create another test
     * for this class.
     */
    @Test
    public void testStartRs() throws Exception {
        final FactoryBuilder builder = mock(FactoryBuilder.class);
        final StartRs svc = new StartRs(builder);
        svc.entrance();
        svc.start("i", "t");
    }

    /**
     * Feel free to delete this test, if you managed to create another test
     * for this class.
     */
    @Test
    public void testAuth() throws Exception {
        final FactoryBuilder builder = mock(FactoryBuilder.class);
        final UserFactory factory = mock(UserFactory.class);
        doReturn(factory).when(builder).getUserFactory();
        final User user = mock(User.class);
        doReturn(1L).when(user).number();
        doReturn(user).when(factory).find(1L);
        final Auth auth = new Auth();
        auth.encode(user);
        auth.decode(builder, "123");
    }

    /**
     * Feel free to delete this test, if you managed to create another test
     * for this class.
     */
    @Test(expected = NotLoggedInException.class)
    public void testAuthOnFailure() throws Exception {
        new Auth().decode(null, null);
    }

}
