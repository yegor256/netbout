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

import com.netbout.hub.Hub;
import com.netbout.hub.HubMocker;
import com.netbout.spi.Bout;
import com.netbout.spi.BoutMocker;
import com.netbout.spi.Identity;
import com.netbout.spi.IdentityMocker;
import javax.ws.rs.core.Response;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link StageRs}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class StageRsTest {

    /**
     * StageRs can render front page of a bout.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void returnsResourceContent() throws Exception {
        final Identity identity = new IdentityMocker().mock();
        final Bout bout = new BoutMocker()
            .withParticipant(identity)
            .mock();
        Mockito.doReturn(bout).when(identity).start();
        Mockito.doReturn(bout).when(identity).bout(Mockito.any(Long.class));
        final Hub hub = new HubMocker()
            .doReturn(
                "Content-type: text/xml\n\n<page><text>hi</text></page>",
                "render-stage-resource"
        )
            .mock();
        final BoutRs root = new NbResourceMocker()
            .withHub(hub)
            .withIdentity(identity)
            .mock(BoutRs.class);
        final StageCoordinates coords = new StageCoordinates();
        final StageRs rest = new StageRs(bout, coords);
        rest.duplicate(root);
        final Response response = rest.get("some-path.xml");
        MatcherAssert.assertThat(
            response.getEntity().toString(),
            Matchers.equalTo("<page><text>hi</text></page>")
        );
    }

}
