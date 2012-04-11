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
package com.netbout.inf.motors.bundles;

import com.netbout.inf.Atom;
import com.netbout.inf.Pointer;
import com.netbout.inf.Predicate;
import com.netbout.inf.PredicateException;
import com.netbout.inf.atoms.NumberAtom;
import com.netbout.inf.triples.HsqlTriples;
import com.netbout.inf.triples.Triples;
import com.netbout.spi.Message;
import com.netbout.spi.Participant;
import com.netbout.spi.Urn;
import com.ymock.util.Logger;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Bundles motor.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class BundlesMotor implements Pointer {

    /**
     * Message to bout (name of triple).
     */
    @SuppressWarnings("PMD.DefaultPackage")
    static final String MSG_TO_BOUT = "message-to-bout";

    /**
     * Bout to marker (name of triple).
     */
    @SuppressWarnings("PMD.DefaultPackage")
    static final String BOUT_TO_MARKER = "bout-to-marker";

    /**
     * The triples.
     */
    private final transient Triples triples;

    /**
     * Public ctor.
     * @param dir The directory to work in
     */
    public BundlesMotor(final File dir) {
        this.triples = new HsqlTriples(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String statistics() {
        return this.getClass().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "BundlesMotor";
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
        return name.matches("bundled|unbundled");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate build(final String name, final List<Atom> atoms) {
        Predicate pred;
        if ("bundled".equals(name)) {
            pred = new BundledPred(this.triples);
        } else if ("unbundled".equals(name)) {
            String marker;
            try {
                marker = this.triples.<String>get(
                    ((NumberAtom) atoms.get(0)).value(),
                    BundlesMotor.BOUT_TO_MARKER
                );
            } catch (com.netbout.inf.triples.MissedTripleException ex) {
                throw new PredicateException(ex);
            }
            pred = new UnbundledPred(
                this.triples,
                marker,
                ((NumberAtom) atoms.get(0)).value()
            );
        } else {
            throw new PredicateException(
                String.format("Predicate %s not supported in BUNDLES", name)
            );
        }
        return pred;
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
        final Set<Urn> names = new TreeSet<Urn>();
        for (Participant dude : msg.bout().participants()) {
            names.add(dude.identity().name());
        }
        final String marker = Logger.format("%[list]s", names);
        this.triples.put(
            msg.bout().number(),
            BundlesMotor.BOUT_TO_MARKER,
            marker
        );
    }

}
