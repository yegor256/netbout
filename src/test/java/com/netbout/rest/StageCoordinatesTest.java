/**
 * Copyright (c) 2009-2014, Netbout.com
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

import com.jcabi.urn.URN;
import com.jcabi.urn.URNMocker;
import com.netbout.hub.Hub;
import com.netbout.hub.HubMocker;
import com.netbout.spi.Bout;
import com.netbout.spi.BoutMocker;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link StageCoordinates}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class StageCoordinatesTest {

    /**
     * StageCoordinates can throw exception if no normalization happens.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = IllegalStateException.class)
    public void doesntAllowToWorkWithoutNormalization() throws Exception {
        final URN stage = new URNMocker().mock();
        final String place = "/some/place?with&some info";
        final StageCoordinates coords = new StageCoordinates();
        coords.setStage(stage);
        coords.setPlace(place);
        coords.stage();
    }

    /**
     * StageCoordinates can normalize location when bout is empty.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void normalizesWithEmptyBout() throws Exception {
        final URN stage = new URNMocker().mock();
        final String place = "/some/place";
        final StageCoordinates coords = new StageCoordinates();
        coords.setStage(stage);
        coords.setPlace(place);
        coords.normalize(new HubMocker().mock(), Mockito.mock(Bout.class));
        MatcherAssert.assertThat(coords.stage().isEmpty(), Matchers.is(true));
    }

    /**
     * StageCoordinates can convert text (cookie) to coordinates and back.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void convertsTextToCoordinatesAndBack() throws Exception {
        final URN stage = new URNMocker().mock();
        final String place = "/some/place?with-info";
        final StageCoordinates coords = new StageCoordinates();
        coords.setStage(stage);
        coords.setPlace(place);
        final Hub hub = new HubMocker()
            .doReturn(true, "does-stage-exist")
            .mock();
        final Bout bout = new BoutMocker()
            .withParticipant(stage.toString())
            .mock();
        coords.normalize(hub, bout);
        final String text = coords.toString();
        final StageCoordinates reverted = StageCoordinates.valueOf(text);
        reverted.normalize(hub, bout);
        MatcherAssert.assertThat(reverted.stage(), Matchers.equalTo(stage));
        MatcherAssert.assertThat(reverted.place(), Matchers.equalTo(place));
    }

    /**
     * StageCoordinates can handle incorrect format of input properly.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void handlesIncorrectFormatProperly() throws Exception {
        final Hub hub = new HubMocker().mock();
        final Bout bout = new BoutMocker().mock();
        final StageCoordinates coords = StageCoordinates.valueOf("ouch");
        coords.normalize(hub, bout);
        MatcherAssert.assertThat(coords.stage(), Matchers.equalTo(new URN()));
    }

    /**
     * StageCoordinates can set place to "empty" when stage is changed.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void resetsPlaceWhenStageIsChanging() throws Exception {
        final Hub hub = new HubMocker().mock();
        final Bout bout = new BoutMocker().mock();
        final StageCoordinates coords = new StageCoordinates();
        coords.normalize(hub, bout);
        coords.setPlace("abc");
        coords.setStage(new URN("urn:test:"));
        MatcherAssert.assertThat(coords.place(), Matchers.equalTo(""));
    }

}
