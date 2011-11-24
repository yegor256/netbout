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
package com.netbout.rest;

import com.netbout.hub.HubEntry;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Participant;
import com.netbout.spi.cpa.CpaHelper;
import com.netbout.spi.cpa.Farm;
import com.netbout.spi.cpa.IdentityAware;
import com.netbout.spi.cpa.Operation;
import java.util.ArrayList;
import java.util.Collection;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link StageCoordinates}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class StageCoordinatesTest {

    /**
     * The helper.
     */
    private static Identity helper;

    /**
     * Register helper.
     * @throws Exception If there is some problem inside
     */
    @BeforeClass
    public static void registerHelper() throws Exception {
        StageCoordinatesTest.helper = HubEntry.user("bar").identity("123");
        StageCoordinatesTest.helper.promote(
            new CpaHelper(StageCoordinatesTest.class)
        );
        // persistor.setPhoto(
        //     new java.net.URL("http://img.netbout.com/db.png")
        // );
    }

    /**
     * Text to coordinates and back.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testTextToCoordinatesAndBack() throws Exception {
        final String place = "/some/place?with&some info";
        final StageCoordinates coords = new StageCoordinates();
        coords.setStage(this.helper.name());
        coords.setPlace(place);
        coords.normalize(this.mockBout(this.helper.name()));
        final String text = coords.toString();
        final StageCoordinates reverted = StageCoordinates.valueOf(text);
        reverted.normalize(this.mockBout(this.helper.name(), "somebody else"));
        MatcherAssert.assertThat(
            reverted.stage(),
            Matchers.equalTo(this.helper.name())
        );
        MatcherAssert.assertThat(reverted.place(), Matchers.equalTo(place));
    }

    /**
     * Prepare bout with these participants.
     * @param names Names of identities who take participaiton in the bout
     * @return The bout with participants
     */
    private Bout mockBout(final String... names) {
        final Bout bout = Mockito.mock(Bout.class);
        Mockito.doReturn(1L).when(bout).number();
        final Collection<Participant> dudes = new ArrayList<Participant>();
        Mockito.doReturn(dudes).when(bout).participants();
        for (String name : names) {
            final Identity identity = Mockito.mock(Identity.class);
            Mockito.doReturn(name).when(identity).name();
            final Participant participant = Mockito.mock(Participant.class);
            Mockito.doReturn(identity).when(participant).identity();
            dudes.add(participant);
        }
        return bout;
    }

    /**
     * Foo farm.
     */
    @Farm
    public static final class FooFarm implements IdentityAware {
        /**
         * Me.
         */
        private transient Identity identity;
        /**
         * {@inheritDoc}
         */
        @Override
        public void init(final Identity idnt) {
            this.identity = idnt;
        }
        /**
         * Does this stage exist in the bout?
         * @param number Bout where it is happening
         * @param stage Name of stage to render
         * @return Does it?
         */
        @Operation("does-stage-exist")
        public Boolean doesStageExist(final Long number, final String stage) {
            Boolean exists = null;
            if (this.identity.name().equals(stage)) {
                exists = Boolean.TRUE;
            }
            return exists;
        }
    }

}
