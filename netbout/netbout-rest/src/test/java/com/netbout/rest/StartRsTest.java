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

import com.netbout.engine.Bout;
import com.netbout.engine.BoutFactory;
import com.netbout.engine.Identity;
import com.netbout.engine.User;
import com.netbout.engine.UserFactory;
import com.netbout.rest.jaxb.PageStart;
import java.net.URI;
import java.security.Principal;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.junit.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Ignore
public final class StartRsTest {

    private static final String IDENTITY = "Alex Smith";

    private static final Long BOUT_ID = 123L;

    private static final String BOUT_TITLE = "some text";

    private static final Long USER_ID = 633L;

    @Test
    public void testEntrancePage() throws Exception {
        final UserFactory factory = mock(UserFactory.class);
        final User user = mock(User.class);
        doReturn(user).when(factory).find(this.USER_ID);
        final FactoryBuilder builder = mock(FactoryBuilder.class);
        doReturn(factory).when(builder).getUserFactory();
        final StartRs svc = new StartRs(builder);
        assertThat(svc.entrance(), instanceOf(PageStart.class));
    }

    @Test
    public void testBoutCreatingPage() throws Exception {
        // bouts
        final BoutFactory bfactory = mock(BoutFactory.class);
        final Bout bout = mock(Bout.class);
        doReturn(this.BOUT_ID).when(bout).number();
        doReturn(bout).when(bfactory)
            .create((Identity) anyObject(), anyString());
        // users
        final UserFactory ufactory = mock(UserFactory.class);
        final User user = mock(User.class);
        doReturn(user).when(ufactory).find(this.USER_ID);
        final FactoryBuilder builder = mock(FactoryBuilder.class);
        doReturn(ufactory).when(builder).getUserFactory();
        doReturn(bfactory).when(builder).getBoutFactory();
        // service
        final StartRs svc = new StartRs(builder);
        final UriInfo uinfo = mock(UriInfo.class);
        final UriBuilder ubuilder = mock(UriBuilder.class);
        doReturn(ubuilder).when(uinfo).getAbsolutePathBuilder();
        doReturn(ubuilder).when(ubuilder)
            .path((Class) anyObject(), anyString());
        final URI uri = new URI("http://localhost/abc");
        doReturn(uri).when(ubuilder).build(anyVararg());
        svc.setUriInfo(uinfo);
        final Response response = svc.start(this.IDENTITY, this.BOUT_TITLE);
    }

}
