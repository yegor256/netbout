/**
 * Copyright (c) 2009-2011, netBout.com
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
package com.netbout.hub.predicates;

import com.netbout.hub.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 * Variable arguments predicate.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public abstract class AbstractVarargPred implements Predicate {

    /**
     * Name of it.
     */
    private final transient String name;

    /**
     * Arguments.
     */
    private final transient List<Predicate> arguments =
        new ArrayList<Predicate>();

    /**
     * Public ctor.
     * @param nam The name of it
     * @param args Arguments/predicates
     */
    public AbstractVarargPred(final String nam, final List<Predicate> args) {
        this.name = nam;
        this.arguments.addAll(args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        return String.format(
            "(%s %s)",
            this.name,
            StringUtils.join(this.args(), " ")
        );
    }

    /**
     * Get arguments.
     * @return The arguments
     */
    protected final List<Predicate> args() {
        return this.arguments;
    }

    /**
     * Get argument by number.
     * @param num The number
     * @return The predicate/argument
     */
    protected final Predicate arg(final int num) {
        return this.arguments.get(num);
    }

}
