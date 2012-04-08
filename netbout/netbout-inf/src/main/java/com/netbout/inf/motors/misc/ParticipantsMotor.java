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

import com.netbout.spi.Message;
import com.netbout.spi.NetboutUtils;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.List;
import org.reflections.Reflections;

/**
 * Participants motor.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class ParticipantsMotor implements Pointer {

    /**
     * Message to bout (name of triple).
     */
    private static final String MSG_TO_BOUT = "message-to-bout";

    /**
     * Bout to participant (name of triple).
     */
    private static final String BOUT_TO_PARTICIPANT = "bout-to-participant";

    /**
     * The triples.
     */
    private final transient Triples triples;

    /**
     * Public ctor.
     * @param dir The directory to work in
     */
    public ParticipantsMotor(final File dir) {
        this.triples = new Triples(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Participants";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws java.io.IOException {
        this.triples.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean pointsTo(final String name) {
        return name.matches("talks-with");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate build(final String name, final List<Atom> atoms) {
        return new TalksWithPred(
            this.triples,
            Urn.create(((TextAtom) atoms.get(0)).value())
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void see(final Message msg) {
        this.triples.put(
            msg.number(),
            BundlesMotor.MSG_TO_BOUT,
            msg.bout().number()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void see(final Bout bout) {
        this.triples.remove(bout.number(), BundlesMotor.BOUT_TO_PARTICIPANT);
        for (Participant dude : bout.participants()) {
            this.triples.put(
                bout.number(),
                BundlesMotor.BOUT_TO_PARTICIPANT,
                dude.identity().name()
            );
        }
    }

}
