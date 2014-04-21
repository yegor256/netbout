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
package com.netbout.rest;

import com.netbout.hub.HubMocker;
import com.netbout.spi.Bout;
import com.netbout.spi.BoutMocker;
import com.netbout.spi.Identity;
import com.netbout.spi.IdentityMocker;
import com.rexsl.test.XhtmlMatchers;
import javax.ws.rs.core.Response;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link BoutRs}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class BoutRsTest {

    /**
     * BoutRs can render front page of a bout.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void rendersBoutFrontPage() throws Exception {
        final String title = "\u0443\u0440\u0430!";
        final Identity identity = new IdentityMocker().mock();
        final Bout bout = new BoutMocker()
            .withParticipant(identity.name())
            .titledAs(title)
            .mock();
        Mockito.doReturn(bout).when(identity).start();
        Mockito.doReturn(bout).when(identity).bout(Mockito.any(Long.class));
        final BoutRs rest = new NbResourceMocker()
            .withIdentity(identity)
            .withHub(
                new HubMocker()
                    .withIdentity(identity.name(), identity)
                    .mock()
            )
            .mock(BoutRs.class);
        rest.setNumber(bout.number());
        rest.setPeriod("10-20");
        rest.setStage(null);
        rest.setPlace(null);
        rest.setQuery("hello");
        rest.setMask(null);
        rest.setStageCoords(null);
        final Response response = rest.front();
        MatcherAssert.assertThat(
            NbResourceMocker.the((NbPage) response.getEntity(), rest),
            XhtmlMatchers.hasXPaths(
                "/page/bout/participants/participant/identity",
                String.format("/page/bout[title='%s']", title),
                "/page/links/link[@rel='top']",
                "/page/links/link[@rel='leave']",
                "/page/links/link[@rel='post']",
                "/page/links/link[@rel='search']",
                "/page/links/link[@rel='about']",
                "/page[query='hello']",
                "/page/bout[view='10-20']"
            )
        );
    }

}
