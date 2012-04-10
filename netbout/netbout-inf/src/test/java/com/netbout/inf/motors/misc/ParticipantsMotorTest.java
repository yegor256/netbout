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
package com.netbout.inf.motors.misc;

import com.netbout.inf.Atom;
import com.netbout.inf.Index;
import com.netbout.inf.IndexMocker;
import com.netbout.inf.Predicate;
import com.netbout.inf.atoms.TextAtom;
import com.netbout.spi.Bout;
import com.netbout.spi.BoutMocker;
import com.netbout.spi.Message;
import com.netbout.spi.MessageMocker;
import com.netbout.spi.Urn;
import com.netbout.spi.UrnMocker;
import java.util.Arrays;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case of {@link ParticipantsMotor}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class ParticipantsMotorTest {

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public transient TemporaryFolder dir = new TemporaryFolder();

    /**
     * ParticipantsMotor can match a message with participant.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void positivelyMatchesMessageWithParticipant() throws Exception {
        final Urn name = new UrnMocker().mock();
        final Bout bout = new BoutMocker()
            .withParticipant(name)
            .mock();
        final Message message = new MessageMocker()
            .inBout(bout)
            .mock();
        final Pointer motor = new ParticipantsMotor(this.dir);
        final Predicate pred = motor.build(
            "talks-with",
            Arrays.asList(new Atom[] {new TextAtom(name.toString())})
        );
        MatcherAssert.assertThat("has next", pred.hasNext());
        MatcherAssert.assertThat(
            pred.next(),
            Matchers.equalTo(message.number())
        );
        MatcherAssert.assertThat("end of iterator", !pred.hasNext());
        MatcherAssert.assertThat("matched", pred.contains(message.number()));
    }

}
