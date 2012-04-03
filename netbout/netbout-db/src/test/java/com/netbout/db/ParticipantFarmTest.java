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
package com.netbout.db;

import com.netbout.spi.Urn;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link ParticipantFarm}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class ParticipantFarmTest {

    /**
     * Farm to work with.
     */
    private final transient ParticipantFarm farm = new ParticipantFarm();

    /**
     * ParticipantFarm can manipulate participant status.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void manipulatesWithStatus() throws Exception {
        final Long bout = new BoutRowMocker().mock();
        final Urn identity = new ParticipantRowMocker(bout).mock();
        MatcherAssert.assertThat(
            this.farm.getParticipantStatus(bout, identity),
            Matchers.equalTo(false)
        );
        this.farm.changedParticipantStatus(bout, identity, true);
        MatcherAssert.assertThat(
            this.farm.getParticipantStatus(bout, identity),
            Matchers.equalTo(true)
        );
    }

    /**
     * ParticipantFarm can manipulate with leadership status of participant.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void manipulatesWithLeadership() throws Exception {
        final Long bout = new BoutRowMocker().mock();
        final Urn identity = new ParticipantRowMocker(bout).mock();
        MatcherAssert.assertThat(
            this.farm.getParticipantLeadership(bout, identity),
            Matchers.equalTo(false)
        );
        this.farm.changedParticipantLeadership(bout, identity, true);
        MatcherAssert.assertThat(
            this.farm.getParticipantLeadership(bout, identity),
            Matchers.equalTo(true)
        );
    }

    /**
     * ParticipantFarm can read all participants of the bout.
     * @throws Exception If there is some problem inside
     */
    @Test
    @org.junit.Ignore
    public void readsBoutParticipants() throws Exception {
        final Long bout = new BoutRowMocker().mock();
        final Urn identity = new ParticipantRowMocker(bout).mock();
        final List<Urn> names = this.farm.getBoutParticipants(bout);
        MatcherAssert.assertThat(
            names,
            Matchers.hasItem(identity)
        );
    }

    /**
     * ParticipantFarm can remove bout participants.
     * @throws Exception If there is some problem inside
     */
    @Test
    @org.junit.Ignore
    public void addsAndRemovesParticipants() throws Exception {
        final Long bout = new BoutRowMocker().mock();
        final Urn identity = new ParticipantRowMocker(bout).mock();
        this.farm.removedBoutParticipant(bout, identity);
        final List<Urn> names = this.farm.getBoutParticipants(bout);
        MatcherAssert.assertThat(names.size(), Matchers.equalTo(0));
    }

}
