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
package com.netbout.queue;

import com.netbout.spi.Bout;
import com.netbout.spi.Token;
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
@SuppressWarnings("PMD.TooManyMethods")
public final class Transaction implements Token {

    /**
     * Mnemo.
     */
    private final transient String imnemo;

    /**
     * Priority.
     */
    private transient HelpQueue.Priority ipriority;

    /**
     * Default value.
     */
    private transient String def = TypeMapper.TEXT_NULL;

    /**
     * Arguments.
     */
    private final transient List<String> iargs = new ArrayList<String>();

    /**
     * The result.
     */
    private transient String iresult;

    /**
     * Public ctor.
     * @param mnemo Mnemo-code of the request
     */
    public Transaction(final String mnemo) {
        this.imnemo = mnemo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String mnemo() {
        return this.imnemo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String arg(final int pos) {
        return this.iargs.get(pos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void result(final String result) {
        this.iresult = result;
    }

    /**
     * Set priority.
     * @param priority Priority
     * @return This object
     */
    public Transaction priority(final HelpQueue.Priority priority) {
        this.ipriority = priority;
        return this;
    }

    /**
     * Set scope, if necessary.
     * @param bout The bout where this transaction is happening
     * @return This object
     */
    public Transaction inBout(final Bout bout) {
        // tbd
        return this;
    }

    /**
     * Set progress report.
     * @param report The report
     * @return This object
     */
    public Transaction progressReport(final ProgressReport report) {
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
            this.iargs.add(TypeMapper.toText(arg));
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
        assert this.ipriority != null;
        HelpQueue.execute(this);
        if (this.iresult == null || TypeMapper.TEXT_NULL.equals(this.iresult)) {
            this.iresult = this.def;
        }
        Logger.debug(
            this,
            "#exec(%s, %s): returned '%s' for [%s]",
            this.mnemo(),
            type.getName(),
            this.iresult,
            this.argsAsText()
        );
        try {
            return (T) TypeMapper.toObject(this.iresult, type);
        } catch (com.netbout.spi.HelperException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Execute and return nothing.
     */
    public void exec() {
        HelpQueue.execute(this);
        Logger.debug(
            Transaction.class,
            "#exec(%s): done for [%s]",
            this.mnemo(),
            this.argsAsText()
        );
    }

    /**
     * Arguments as text, for logging.
     * @return The text
     */
    private String argsAsText() {
        return String.format(
            "'%s'",
            StringUtils.join(this.iargs, "', '")
        );
    }

}
