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
import com.netbout.inf.Predicate;
import com.netbout.inf.PredicateException;
import com.netbout.inf.atoms.NumberAtom;
import com.netbout.inf.triples.Triples;
import java.util.List;

/**
 * Allows only messages that unbundle on the specified bout number.
 *
 * <p>This class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class UnbundledPred implements Predicate {

    /**
     * Triples to use.
     */
    private final transient Triples triples;

    /**
     * Expected bundle.
     */
    private final transient String bundle;

    /**
     * Current bout, to ignore.
     */
    private final transient Long bout;

    /**
     * Public ctor.
     * @param trpls The triples to use
     * @param bndl The bundle to use
     * @param bot Current bout number to ignore
     */
    public UnbundledPred(final Triples trpls, final String bndl,
        final Long bot) {
        this.triples = trpls;
        this.bundle = bndl;
        this.bout = bot;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long next() {
        throw new PredicateException("UNBUNDLED#next()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        throw new PredicateException("UNBUNDLED#hasNext()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final Long message) {
        final String marker = this.triples.get(
            this.triples.<Long>get(message, BundlesMotor.MSG_TO_BOUT),
            BundlesMotor.BOUT_TO_MARKER
        );
        return this.bundle.equals(marker)
            && !this.triples.<Long>get(message, BundlesMotor.MSG_TO_BOUT)
                .equals(this.bout);
    }

}
