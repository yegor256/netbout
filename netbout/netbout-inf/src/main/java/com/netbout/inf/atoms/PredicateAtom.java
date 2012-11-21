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
 * this code accidentally and without intent to use it, please report this
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
import com.netbout.inf.Functor;
import com.netbout.inf.InvalidSyntaxException;
import com.netbout.inf.Ray;
import com.netbout.inf.Term;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang.StringUtils;

/**
 * Predicate atom.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class PredicateAtom implements Atom<String> {

    /**
     * The functor.
     */
    private final transient Functor functor;

    /**
     * The name of it.
     */
    private final transient String name;

    /**
     * List of atoms.
     */
    private final transient List<Atom<?>> args;

    /**
     * Public ctor.
     * @param txt Name of it
     * @param atoms Arguments
     * @param fnctr The functor
     */
    public PredicateAtom(@NotNull final String txt,
        @NotNull final List<Atom<?>> atoms, @NotNull final Functor fnctr) {
        this.name = txt;
        this.args = new ArrayList<Atom<?>>(atoms);
        this.functor = fnctr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.name.hashCode() + this.args.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return obj == this || (obj instanceof PredicateAtom
            && this.hashCode() == obj.hashCode());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "(%s %s)",
            this.name,
            StringUtils.join(this.args, " ")
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get term from it.
     * @param ray The ray to use
     * @return The term
     * @throws InvalidSyntaxException If can't build it
     */
    public Term term(@NotNull final Ray ray) throws InvalidSyntaxException {
        return this.functor.build(ray, this.args);
    }

}
