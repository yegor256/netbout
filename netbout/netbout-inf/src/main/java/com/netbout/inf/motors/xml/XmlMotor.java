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

import com.netbout.inf.Atom;
import com.netbout.inf.Pointer;
import com.netbout.inf.Predicate;
import com.netbout.inf.atoms.TextAtom;
import com.netbout.inf.triples.BerkleyTriples;
import com.netbout.inf.triples.Triples;
import com.netbout.spi.Bout;
import com.netbout.spi.Message;
import com.netbout.spi.Urn;
import com.netbout.spi.xml.DomParser;
import com.ymock.util.Logger;
import java.io.File;
import java.util.List;

/**
 * XML motor.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class XmlMotor implements Pointer {

    /**
     * Message to namespace (name of triple).
     */
    static final String MSG_TO_NS = "message-to-namespace";

    /**
     * The triples.
     */
    final transient Triples triples;

    /**
     * Public ctor.
     * @param dir The directory to work in
     */
    public XmlMotor(final File dir) {
        this.triples = new BerkleyTriples(dir);
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
        return "XML";
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
        return name.matches("ns");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate build(final String name, final List<Atom> atoms) {
        return new NsPred(
            this.triples,
            Urn.create(((TextAtom) atoms.get(0)).value())
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void see(final Message msg) {
        final DomParser parser = new DomParser(msg.text());
        if (parser.isXml()) {
            Urn namespace;
            try {
                this.triples.put(
                    msg.number(),
                    XmlMotor.MSG_TO_NS,
                    parser.namespace().toString()
                );
            } catch (com.netbout.spi.xml.DomValidationException ex) {
                Logger.warn(
                    NsPred.class,
                    "#see(#%d, ..): %[exception]s",
                    msg.number(),
                    ex
                );
            }
        }
    }

}
