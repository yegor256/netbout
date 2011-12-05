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

import com.netbout.bus.Bus;
import com.netbout.bus.BusMocker;
import com.netbout.hub.Hub;
import com.netbout.spi.Bout;
import com.netbout.spi.BoutMocker;
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
     * StageCoordinates can accept changes by setters and return them with
     * getters.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void setsValuesThroughSettersAndReturnsByGetters() throws Exception {
        final String stage = "some-stage-name";
        final String place = "/some/place?with&some info";
        final StageCoordinates coords = new StageCoordinates();
        coords.setStage(stage);
        coords.setPlace(place);
        MatcherAssert.assertThat(coords.stage(), Matchers.equalTo(stage));
        MatcherAssert.assertThat(coords.place(), Matchers.equalTo(place));
    }

    /**
     * StageCoordinates can normalize location when bout is empty.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void normalizesWithEmptyBout() throws Exception {
        final String stage = "some-stage-name-2";
        final String place = "/some/place";
        final StageCoordinates coords = new StageCoordinates();
        coords.setStage(stage);
        coords.setPlace(place);
        coords.normalize(new BusMocker().mock(), Mockito.mock(Bout.class));
        MatcherAssert.assertThat(coords.stage(), Matchers.equalTo(""));
        MatcherAssert.assertThat(coords.place(), Matchers.equalTo(""));
    }

    /**
     * StageCoordinates can convert text (cookie) to coordinates and back.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void convertsTextToCoordinatesAndBack() throws Exception {
        final String stage = "some-stage-name-3";
        final String place = "/some/place?with-info";
        final StageCoordinates coords = new StageCoordinates();
        coords.setStage(stage);
        coords.setPlace(place);
        final Bus bus = new BusMocker()
            .doReturn(true, "does-stage-exist")
            .mock();
        final Bout bout = new BoutMocker()
            .withParticipant(stage)
            .mock();
        coords.normalize(bus, bout);
        final String text = coords.toString();
        final StageCoordinates reverted = StageCoordinates.valueOf(text);
        MatcherAssert.assertThat(reverted.stage(), Matchers.equalTo(stage));
        MatcherAssert.assertThat(reverted.place(), Matchers.equalTo(place));
    }

}
