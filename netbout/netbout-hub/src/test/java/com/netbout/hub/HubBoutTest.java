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
package com.netbout.hub;

import com.netbout.bus.Bus;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case of {@link HubBout}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class HubBoutTest {

    /**
     * Name of viewer.
     */
    private final String name =
        String.valueOf(Math.abs(new Random().nextLong()));

    /**
     * The viewer.
     */
    private final Identity viewer = Mockito.mock(Identity.class);

    /**
     * The bout data type to work with.
     */
    private final BoutDtMocker boutDtMocker = new BoutDtMocker();

    /**
     * The catalog.
     */
    private final Catalog catalog = Mockito.mock(Catalog.class);

    /**
     * Prepare all mocks.
     * @throws Exception If there is some problem inside
     */
    @Before
    public void prepare() throws Exception {
        Mockito.doReturn(this.name).when(this.viewer).name();
        Mockito.doReturn(this.viewer).when(this.catalog).make(this.name);
        this.boutDtMocker.withParticipant(
            new ParticipantDtMocker()
                .withIdentity(this.name)
                .confirmed()
                .mock()
        );
    }

    /**
     * HubBout can "wrap" BoutDt class.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void wrapsBoutDtDataProperties() throws Exception {
        final Bus bus = new BusMocker().mock();
        final BoutDt data = this.boutDtMocker.mock();
        final Bout bout = new HubBout(this.catalog, bus, this.viewer, data);
        bout.number();
        Mockito.verify(data).getNumber();
        bout.title();
        Mockito.verify(data).getTitle();
    }

    /**
     * HubBout can "wrap" BoutDt renaminng mechanism.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void wrapsBoutRenamingMechanism() throws Exception {
        final Bus bus = new BusMocker().mock();
        final BoutDt data = this.boutDtMocker.mock();
        final Bout bout = new HubBout(this.catalog, bus, this.viewer, data);
        final String title = "some title, no matter which one..";
        bout.rename(title);
        Mockito.verify(data).setTitle(title);
    }

    /**
     * HubBout can accept invitation requests and add participants to the bout.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void acceptsInvitationRequestsAndPassesThemToDt() throws Exception {
        final Bus bus = new BusMocker().mock();
        final BoutDt data = this.boutDtMocker.mock();
        final Bout bout = new HubBout(this.catalog, bus, this.viewer, data);
        final Identity friend = Mockito.mock(Identity.class);
        final String fname = String.valueOf(Math.abs(new Random().nextLong()));
        Mockito.doReturn(fname).when(friend).name();
        bout.invite(friend);
        Mockito.verify(data).addParticipant(fname);
    }

}
