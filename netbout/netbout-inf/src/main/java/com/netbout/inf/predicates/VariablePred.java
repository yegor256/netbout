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

import com.netbout.inf.Meta;
import com.netbout.inf.Msg;
import com.netbout.inf.Predicate;
import com.netbout.inf.PredicateException;
import com.netbout.spi.Message;
import com.netbout.spi.NetboutUtils;
import com.netbout.spi.Urn;
import java.util.Date;

/**
 * Variable.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Meta(extracts = true)
@SuppressWarnings("PMD.CyclomaticComplexity")
public final class VariablePred implements Predicate {

    /**
     * Message property.
     */
    public static final String TEXT = "text";

    /**
     * Message property "date".
     */
    public static final String DATE = "date";

    /**
     * Message property.
     */
    public static final String NUMBER = "number";

    /**
     * Message property.
     */
    public static final String BOUT_NUMBER = "bout.number";

    /**
     * Message property.
     */
    public static final String BOUT_DATE = "bout.date";

    /**
     * Message property.
     */
    public static final String BOUT_RECENT = "bout.recent";

    /**
     * Message property.
     */
    public static final String BOUT_TITLE = "bout.title";

    /**
     * Message property.
     */
    public static final String AUTHOR_NAME = "author.name";

    /**
     * Message property.
     */
    public static final String AUTHOR_ALIAS = "author.alias";

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
     * Extracts necessary data from from.
     * @param from The message to extract from
     * @param msg Where to extract to
     */
    public static void extract(final Message from, final Msg msg) {
        msg.put(VariablePred.TEXT, from.text());
        msg.put(VariablePred.DATE, from.date());
        msg.put(VariablePred.AUTHOR_NAME, from.author().name());
        msg.put(
            VariablePred.AUTHOR_ALIAS,
            NetboutUtils.aliasOf(from.author())
        );
        msg.put(VariablePred.BOUT_DATE, from.bout().date());
        msg.put(VariablePred.BOUT_TITLE, from.bout().title());
        msg.put(VariablePred.BOUT_RECENT, NetboutUtils.dateOf(from.bout()));
    }

    /**
     * {@inheritDoc}
     * @checkstyle CyclomaticComplexity (35 lines)
     */
    @Override
    public Object evaluate(final Msg msg, final int pos) {
        Object value;
        if (this.TEXT.equals(this.name)) {
            value = msg.<String>get(this.TEXT);
        } else if (this.BOUT_NUMBER.equals(this.name)) {
            value = msg.bout();
        } else if (this.BOUT_DATE.equals(this.name)) {
            value = msg.<Date>get(this.BOUT_DATE);
        } else if (this.BOUT_RECENT.equals(this.name)) {
            value = msg.<Date>get(this.BOUT_RECENT);
        } else if (this.BOUT_TITLE.equals(this.name)) {
            value = msg.<String>get(this.BOUT_TITLE);
        } else if (this.NUMBER.equals(this.name)) {
            value = msg.number();
        } else if (this.DATE.equals(this.name)) {
            value = msg.<Date>get(this.DATE);
        } else if (this.AUTHOR_NAME.equals(this.name)) {
            value = msg.<Urn>get(this.AUTHOR_NAME);
        } else if (this.AUTHOR_ALIAS.equals(this.name)) {
            value = msg.<String>get(this.AUTHOR_ALIAS);
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
