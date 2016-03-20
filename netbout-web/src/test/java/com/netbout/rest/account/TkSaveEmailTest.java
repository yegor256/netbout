/**
 * Copyright (c) 2009-2016, netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netbout Inc. located at www.netbout.com.
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
package com.netbout.rest.account;

import com.jcabi.email.Postman;
import com.jcabi.urn.URN;
import com.netbout.cached.CdBase;
import com.netbout.email.EmAlias;
import com.netbout.mock.MkBase;
import com.netbout.spi.Alias;
import com.netbout.spi.Base;
import com.netbout.spi.User;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.takes.facets.auth.Identity;
import org.takes.facets.auth.PsFixed;
import org.takes.facets.auth.TkAuth;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqFake;
import org.takes.rq.RqForm;

/**
 * Test case for {@link TkSaveEmail}.
 * @author Dragan Bozanovic (bozanovicdr@gmail.com)
 * @version $Id$
 * @since 2.22
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkSaveEmailTest {
    /**
     * Email parameter name.
     */
    private static final transient String EMAIL = "email";

    /**
     * TkSaveEmail can save email.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void savesEmail() throws Exception {
        final Base base = new CdBase(new MkBase());
        final String urn = "urn:test:1";
        final User user = base.user(new URN(urn));
        user.aliases().add("alias");
        final Alias alias = user.aliases().iterate().iterator().next();
        alias.email("old@example.com");
        new TkAuth(
            new TkSaveEmail(base, false),
            new PsFixed(new Identity.Simple(urn))
        ).act(
            new RqForm.Fake(
                new RqFake(), TkSaveEmailTest.EMAIL, "new@example.com"
            )
        );
        MatcherAssert.assertThat(
            alias.email(),
            Matchers.equalTo("old@example.com!new@example.com")
        );
    }

    /**
     * TkSaveEmail can save email locally.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void savesEmailLocal() throws Exception {
        final MkBase base = new MkBase();
        final String urn = "urn:test:2";
        final User user = base.user(new URN(urn));
        user.aliases().add("alias2");
        final Alias alias = user.aliases().iterate().iterator().next();
        alias.email("jack@example.com");
        final String email = "john@example.com";
        new TkAuth(
            new TkSaveEmail(base),
            new PsFixed(new Identity.Simple(urn))
        ).act(new RqForm.Fake(new RqFake(), TkSaveEmailTest.EMAIL, email));
        MatcherAssert.assertThat(
            alias.email(),
            Matchers.equalTo(email)
        );
    }

    /**
     * TkSaveEmailTest throws RsForward error.
     * @throws Exception on Error.
     */
    @Test(expected = RsForward.class)
    public void throwsRsForward() throws Exception {
        final Postman postman = Mockito.mock(Postman.class);
        final MkBase base = new MkBase();
        final String urn = "urn:test:3";
        final Alias alias = new EmAlias(base.randomAlias(), postman);
        alias.email("email@example.com");
        new TkAuth(
            new TkSaveEmail(base, true),
            new PsFixed(new Identity.Simple(urn))
        ).act(
            new RqForm.Fake(
                new RqFake(), TkSaveEmailTest.EMAIL, "new"
                )
        );
    }

}
