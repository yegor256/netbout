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

import com.netbout.engine.BoutFactory;
import com.netbout.engine.User;
import com.netbout.engine.UserFactory;
import com.netbout.rest.jaxb.PageStart;
import javax.ws.rs.core.Response;
import org.junit.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class StartRsTest {

    private static final String IDENTITY = "Alex Smith";

    private static final Long BOUT_ID = 123L;

    private static final String BOUT_TITLE = "some text";

    private static final String USER_LOGIN = "alex@example.com";

    private static final String USER_PWD = "secret77";

    @Test
    public void testEntrancePage() throws Exception {
        final BoutFactory factory = mock(BoutFactory.class);
        // doReturn(bout).when(factory).find(this.BOUT_ID);
        final FactoryBuilder builder = mock(FactoryBuilder.class);
        doReturn(factory).when(builder).getBoutFactory();
        final StartRs svc = new StartRs(builder);
        assertThat(svc.entrance(), instanceOf(PageStart.class));
    }

    @Test
    public void testBoutCreatingPage() throws Exception {
        final BoutFactory bfactory = mock(BoutFactory.class);
        final UserFactory ufactory = mock(UserFactory.class);
        final User user = mock(User.class);
        doReturn(user).when(ufactory).find(this.USER_LOGIN, this.USER_PWD);
        final FactoryBuilder builder = mock(FactoryBuilder.class);
        doReturn(ufactory).when(builder).getUserFactory();
        doReturn(bfactory).when(builder).getBoutFactory();
        final StartRs svc = new StartRs(builder);
        final Response response = svc.start(this.IDENTITY, this.BOUT_TITLE);
        // assertThat(response.entrance(), instanceOf(PageStart.class));
    }

}
