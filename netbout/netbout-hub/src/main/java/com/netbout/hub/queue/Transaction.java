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
package com.netbout.hub.queue;

import com.netbout.spi.TypeMapper;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 * One transaction.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class Transaction {

    /**
     * Mnemo.
     */
    private final String mnemo;

    /**
     * Priority.
     */
    private HelpQueue.Priority priority;

    /**
     * Default value.
     */
    private String def = "NULL";

    /**
     * Arguments.
     */
    private final List<String> args = new ArrayList<String>();

    /**
     * Public ctor.
     * @param text Mnemo-code of the request
     */
    public Transaction(final String text) {
        this.mnemo = text;
    }

    /**
     * Set priority.
     * @param pri Priority
     * @return This object
     */
    public Transaction priority(final HelpQueue.Priority pri) {
        this.priority = pri;
        return this;
    }

    /**
     * Set scope, if necessary.
     * @param number Number of bout where this transaction is happening
     * @return This object
     */
    public Transaction scope(final Long number) {
        // tbd
        return this;
    }

    /**
     * Add argument.
     * @param arg The argument
     * @return This object
     */
    public Transaction arg(final Object arg) {
        try {
            this.args.add(TypeMapper.toText(arg));
        } catch (com.netbout.spi.HelperException ex) {
            throw new IllegalArgumentException(ex);
        }
        return this;
    }

    /**
     * Set default value to return.
     * @param val The value
     * @return This object
     */
    public Transaction asDefault(final Object val) {
        try {
            this.def = TypeMapper.toText(val);
        } catch (com.netbout.spi.HelperException ex) {
            throw new IllegalArgumentException(ex);
        }
        return this;
    }

    /**
     * Execute it and return value.
     * @param type Type of resposne
     * @param <T> Type of response
     * @return The result
     */
    public <T> T exec(final Class<T> type) {
        final String output = HelpQueue.execute(this);
        Object result;
        try {
            result = TypeMapper.toObject(output, type);
        } catch (com.netbout.spi.HelperException ex) {
            throw new IllegalArgumentException(ex);
        }
        Logger.debug(
            Transaction.class,
            "#exec(%s, %s): returned '%s' for [%s]",
            this.mnemo,
            type.getName(),
            output,
            this.argsAsText()
        );
        return (T) result;
    }

    /**
     * Execute and return nothing.
     */
    public void exec() {
        HelpQueue.execute(this);
        Logger.debug(
            Transaction.class,
            "#exec(%s): done for [%s]",
            this.mnemo,
            this.argsAsText()
        );
    }

    /**
     * Get arguments as array.
     * @return The args
     */
    protected String[] getArgs() {
        return this.args.toArray(new String[] {});
    }

    /**
     * Get default result value.
     * @return The value
     */
    protected String getDefault() {
        return this.def;
    }

    /**
     * Get mnemo.
     * @return The mnemo
     */
    protected String getMnemo() {
        return this.mnemo;
    }

    /**
     * Arguments as text, for logging.
     * @return The text
     */
    private String argsAsText() {
        return String.format(
            "'%s'",
            StringUtils.join(this.args, "', '")
        );
    }

}
