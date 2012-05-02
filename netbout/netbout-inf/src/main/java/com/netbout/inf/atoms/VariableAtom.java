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

/**
 * Variable atom.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public enum VariableAtom implements Atom<String> {

    /**
     * Text of message.
     */
    TEXT("text"),

    /**
     * Number of message.
     */
    NUMBER("number"),

    /**
     * Number of bout.
     */
    BOUT_NUMBER("bout.number"),

    /**
     * Title of bout.
     */
    BOUT_TITLE("bout.title"),

    /**
     * Name of author.
     */
    AUTHOR_NAME("author.name"),

    /**
     * Alias of author.
     */
    AUTHOR_ALIAS("author.alias");

    /**
     * The name of it.
     */
    private final transient String name;

    /**
     * Public ctor.
     * @param value The value of it
     */
    VariableAtom(final String value) {
        this.name = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("$%s", this.name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value() {
        throw new UnsupportedOperationException();
    }

    /**
     * Name of attribute for Msg.
     * @return The name
     */
    public String attribute() {
        return String.format("variable-%s", this.name);
    }

    /**
     * Parse text and create variable atom.
     * @return The atom
     */
    public static VariableAtom parse(final String text) {
        VariableAtom atom;
        if (text.equals(VariableAtom.TEXT.name)) {
            atom = VariableAtom.TEXT;
        } else if (text.equals(VariableAtom.NUMBER.name)) {
            atom = VariableAtom.NUMBER;
        } else if (text.equals(VariableAtom.BOUT_NUMBER.name)) {
            atom = VariableAtom.BOUT_NUMBER;
        } else if (text.equals(VariableAtom.BOUT_TITLE.name)) {
            atom = VariableAtom.BOUT_TITLE;
        } else if (text.equals(VariableAtom.AUTHOR_NAME.name)) {
            atom = VariableAtom.AUTHOR_NAME;
        } else if (text.equals(VariableAtom.AUTHOR_ALIAS.name)) {
            atom = VariableAtom.AUTHOR_ALIAS;
        } else {
            throw new IllegalArgumentException(
                String.format("can't parse variable atom '%s'", text)
            );
        }
        return atom;
    }

}
