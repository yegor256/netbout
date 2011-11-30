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
package com.netbout.db;

import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
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
     * Bout number to work with.
     */
    private transient Long bout;

    /**
     * Start new bout to work with.
     * @throws Exception If there is some problem inside
     */
    @Before
    public void prepareNewBout() throws Exception {
        final BoutFarm bfarm = new BoutFarm();
        this.bout = bfarm.getNextBoutNumber();
        bfarm.startedNewBout(this.bout);
    }

    /**
     * Manipulate with participant status.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testBoutStatusChanging() throws Exception {
        final String identity = "Steve Jobs";
        new IdentityFarm().changedIdentityPhoto(identity, "");
        this.farm.addedBoutParticipant(this.bout, identity);
        MatcherAssert.assertThat(
            this.farm.getParticipantStatus(this.bout, identity),
            Matchers.equalTo(false)
        );
        this.farm.changedParticipantStatus(this.bout, identity, true);
        MatcherAssert.assertThat(
            this.farm.getParticipantStatus(this.bout, identity),
            Matchers.equalTo(true)
        );
    }

    /**
     * Read all participants of the bout.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testBoutParticipantsReading() throws Exception {
        final String identity = "Bill Gates";
        new IdentityFarm().changedIdentityPhoto(identity, "");
        this.farm.addedBoutParticipant(this.bout, identity);
        final List<String> names = this.farm.getBoutParticipants(this.bout);
        MatcherAssert.assertThat(
            names,
            Matchers.hasItem(identity)
        );
    }

}
