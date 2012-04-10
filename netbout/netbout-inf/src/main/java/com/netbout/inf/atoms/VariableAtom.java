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
package com.netbout.inf.atoms;

import com.netbout.inf.Atom;
import java.io.Serializable;

/**
 * Variable atom.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class VariableAtom implements Atom, Serializable {

    /**
     * Text of message.
     */
    public static final VariableAtom TEXT =
        new VariableAtom("text");

    /**
     * Number of message.
     */
    public static final VariableAtom NUMBER =
        new VariableAtom("number");

    /**
     * Number of bout.
     */
    public static final VariableAtom BOUT_NUMBER =
        new VariableAtom("bout.number");

    /**
     * Title of bout.
     */
    public static final VariableAtom BOUT_TITLE =
        new VariableAtom("bout.title");

    /**
     * Name of author.
     */
    public static final VariableAtom AUTHOR_NAME =
        new VariableAtom("author.name");

    /**
     * Alias of author.
     */
    public static final VariableAtom AUTHOR_ALIAS =
        new VariableAtom("author.alias");

    /**
     * Serialization marker.
     */
    private static final long serialVersionUID = 0x4255AFCD9812DDEFL;

    /**
     * The name of it.
     */
    @SuppressWarnings("PMD.BeanMembersShouldSerialize")
    private final String name;

    /**
     * Public ctor.
     * @param value The value of it
     */
    public VariableAtom(final String value) {
        this.name = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof VariableAtom
            && this.name.equals(((VariableAtom) obj).name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("$%s", this.name);
    }

}
