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
package com.netbout.rest.bout;

import com.jcabi.urn.URN;
import com.netbout.mock.MkBase;
import com.netbout.spi.Alias;
import com.netbout.spi.Bout;
import com.netbout.spi.User;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.takes.facets.auth.RqWithAuth;
import org.takes.facets.forward.RsFailure;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqFake;
import org.takes.rq.RqMethod;
import org.takes.rq.RqWithHeader;

/**
 * Test case for {@link TkInvite}.
 * @author Endrigo Antonini (teamed@endrigo.com.br)
 * @version $Id$
 * @since 2.15.1
 */
public final class TkInviteTest {

    /**
     * Netbout header identifyer.
     */
    private static final String NETBOUT_HEADER = "X-Netbout-Bout";

    /**
     * Netbout path for invitations.
     */
    private static final String INVITE_PATH = "/b/%d/invite";

    /**
     * TkInvite can invite user by email.
     * @throws Exception If there is some problem inside
     */
    @Test (expected = RsForward.class)
    public void invitesUserByEmail() throws Exception {
        final MkBase base = new MkBase();
        final String urn = "urn:test:1";
        final User user = base.user(new URN(urn));
        user.aliases().add("jeff");
        final Alias alias = user.aliases().iterate().iterator().next();
        final Bout bout = alias.inbox().bout(alias.inbox().start());
        bout.messages().post("Before invite by email.");
        bout.friends().invite(alias.name());
        try {
            new TkInvite(base).act(
                new RqWithHeader(
                    new RqWithAuth(
                        urn,
                        new RqFake(
                            RqMethod.POST,
                            String.format(
                                INVITE_PATH,
                                bout.number()
                            ),
                            "name=foo@bar.airforce"
                        )
                    ),
                    NETBOUT_HEADER,
                    Long.toString(bout.number())
                )
            );
        } catch (final RsForward ex) {
            MatcherAssert.assertThat(
                ex.getLocalizedMessage(),
                Matchers.containsString("foo-bar-airforce")
            );
            throw ex;
        }
    }

    /**
     * TkInvite should throw an RsFailure when invite alias is too long.
     * @throws Exception If there is some problem inside
     */
    @Test (expected = RsFailure.class)
    public void failsWhenInvitingWithTooLongAlias() throws Exception {
        final MkBase base = new MkBase();
        final String urn = "urn:test:2";
        final User user = base.user(new URN(urn));
        final String name = StringUtils.join(
            "12345678901234567890123456789012345678901234",
            "5678901234567890123456789012345678901234567",
            "890123456789012345678901234567890"
        );
        user.aliases().add(name);
        final Alias alias = user.aliases().iterate().iterator().next();
        final Bout bout = alias.inbox().bout(alias.inbox().start());
        try {
            new TkInvite(base).act(
                new RqWithHeader(
                    new RqWithAuth(
                        urn,
                        new RqFake(
                            RqMethod.POST,
                            String.format(
                                INVITE_PATH,
                                bout.number()
                            ),
                            String.format("name=%s", name)
                        )
                    ),
                    NETBOUT_HEADER,
                    Long.toString(bout.number())
                )
            );
        } catch (final RsFailure ex) {
            MatcherAssert.assertThat(
                ex.getLocalizedMessage(),
                Matchers.containsString("incorrect alias")
            );
            throw ex;
        }
    }
}
