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
package com.netbout.inf.predicates.logic;

import com.netbout.inf.Atom;
import com.netbout.inf.Index;
import com.netbout.inf.Meta;
import com.netbout.inf.Predicate;
import com.netbout.inf.PredicateException;
import com.netbout.inf.predicates.AbstractVarargPred;
import java.util.List;

/**
 * Logical OR.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Meta(name = "or")
public final class OrPred extends AbstractVarargPred {

    /**
     * Public ctor.
     * @param args Arguments/predicates
     * @param index The index to use for searching
     */
    public OrPred(final List<Atom> args, final Index index) {
        super(args, index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long next() {
        Long message = null;
        for (Atom pred : this.args()) {
            if (((Predicate) pred).hasNext()) {
                message = ((Predicate) pred).next();
                break;
            }
        }
        if (message == null) {
            throw new PredicateException("end of messsages reached");
        }
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        boolean has = false;
        for (Atom pred : this.args()) {
            has |= ((Predicate) pred).hasNext();
            if (has) {
                break;
            }
        }
        return has;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final Long message) {
        boolean allowed = false;
        for (Atom pred : this.args()) {
            allowed |= ((Predicate) pred).contains(message);
            if (allowed) {
                break;
            }
        }
        return allowed;
    }

}
