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
package com.netbout.inf.predicates;

import com.netbout.inf.Atom;
import com.netbout.inf.Index;
import com.netbout.inf.Meta;
import com.netbout.inf.Predicate;
import com.netbout.inf.PredicateException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 * Variable arguments predicate.
 *
 * <p>This class is NOT thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
abstract class AbstractVarargPred implements Predicate {

    /**
     * Name of it.
     */
    private final transient String iname;

    /**
     * Arguments.
     */
    private final transient List<Atom> atoms;

    /**
     * Public ctor.
     * @param args Arguments/predicates
     */
    public AbstractVarargPred(final List<Atom> args) {
        this.iname = this.getClass().getAnnotation(Meta.class).name();
        this.atoms = new ArrayList<Atom>(args.size());
        this.atoms.addAll(args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String value() {
        throw new PredicateException("#value() not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        return String.format(
            "(%s %s)",
            this.iname,
            StringUtils.join(this.args(), " ")
        );
    }

    /**
     * Get arguments.
     * @return The arguments
     */
    protected final List<Atom> args() {
        return this.atoms;
    }

    /**
     * Get its name.
     * @return The name
     */
    protected final String name() {
        return this.iname;
    }

    /**
     * Get argument by number.
     * @param num The number
     * @return The predicate/argument
     */
    protected final Atom arg(final int num) {
        if (num >= this.atoms.size()) {
            throw new PredicateException(
                String.format("argument #%d is absnet in '%s'", num, this)
            );
        }
        return this.atoms.get(num);
    }

}
