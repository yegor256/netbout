/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi.client;

import com.jcabi.urn.URN;
import com.jcabi.urn.URNMocker;
import com.netbout.spi.Participant;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link RestParticipant}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class RestParticipantTest {

    /**
     * RestParticipant can fetch confirmation status of participant.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void fetchesConfirmationStatusOfParticipant() throws Exception {
        final RestClient client = new RestClientMocker().onXPath(
            // @checkstyle LineLength (1 line)
            "/page/bout/participants/participant[identity='urn:test:A' ]/@confirmed",
            "true"
        ).mock();
        final Participant dude = new RestParticipant(
            client,
            new URN("urn:test:A")
        );
        MatcherAssert.assertThat(dude.confirmed(), Matchers.equalTo(true));
    }

    /**
     * RestParticipant can return a clean name of identity.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void hasToStringWithCleanImplementation() throws Exception {
        final URN name = new URNMocker().mock();
        final Participant dude = new RestParticipant(null, name);
        MatcherAssert.assertThat(
            dude,
            Matchers.hasToString(Matchers.equalTo(name.toString()))
        );
    }

}
