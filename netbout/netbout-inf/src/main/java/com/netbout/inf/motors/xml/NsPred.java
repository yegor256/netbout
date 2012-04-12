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
package com.netbout.inf.motors.xml;

import com.netbout.inf.Predicate;
import com.netbout.inf.triples.Triples;
import com.netbout.spi.Urn;
import java.util.Iterator;

/**
 * Namespace predicate.
 *
 * <p>This class is NOT thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class NsPred implements Predicate {

    /**
     * Triples to use.
     */
    private final transient Triples triples;

    /**
     * Iterator of them.
     */
    private final transient Iterator<Long> iterator;

    /**
     * The namespace.
     */
    private final transient Urn namespace;

    /**
     * Public ctor.
     * @param trp The triples
     * @param nsp The namespace
     */
    public NsPred(final Triples trp, final Urn nsp) {
        this.triples = trp;
        this.namespace = nsp;
        this.iterator = this.triples.reverse(
            XmlMotor.MSG_TO_NS,
            this.namespace.toString()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long next() {
        return this.iterator.next();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        return this.iterator.hasNext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final Long message) {
        boolean contains;
        try {
            contains = Urn.create(
                this.triples.get(message, XmlMotor.MSG_TO_NS)
            ).equals(this.namespace);
        } catch (com.netbout.inf.triples.MissedTripleException ex) {
            contains = false;
        }
        return contains;
    }

}
