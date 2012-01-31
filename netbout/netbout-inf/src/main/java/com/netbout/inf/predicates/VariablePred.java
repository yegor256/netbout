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
package com.netbout.inf.predicates;

import com.netbout.inf.Predicate;
import com.netbout.inf.PredicateException;
import com.netbout.spi.Message;
import com.netbout.spi.NetboutUtils;

/**
 * Variable.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class VariablePred implements Predicate {

    /**
     * The value of it.
     */
    private final transient String name;

    /**
     * Public ctor.
     * @param value The value of it
     */
    public VariablePred(final String value) {
        this.name = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object evaluate(final Message msg, final int pos) {
        Object value;
        if ("text".equals(this.name)) {
            value = msg.text();
        } else if ("bout.number".equals(this.name)) {
            value = msg.bout().number();
        } else if ("bout.date".equals(this.name)) {
            value = msg.bout().date();
        } else if ("bout.recent".equals(this.name)) {
            value = NetboutUtils.dateOf(msg.bout());
        } else if ("bout.title".equals(this.name)) {
            value = msg.bout().title();
        } else if ("number".equals(this.name)) {
            value = msg.number();
        } else if ("date".equals(this.name)) {
            value = msg.date();
        } else if ("author.name".equals(this.name)) {
            value = msg.author().name();
        } else if ("author.alias".equals(this.name)) {
            value = NetboutUtils.aliasOf(msg.author());
        } else if ("seen".equals(this.name)) {
            value = msg.seen();
        } else {
            throw new PredicateException(
                String.format("Unknown variable '$%s'", this.name)
            );
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("$%s", this.name);
    }

}
