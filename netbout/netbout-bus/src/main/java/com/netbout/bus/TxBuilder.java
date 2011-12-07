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
package com.netbout.bus;

import com.netbout.spi.Bout;

/**
 * Transaction builder.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public interface TxBuilder {

    /**
     * Execute it as soon as possible.
     * @return This object
     */
    TxBuilder asap();

    /**
     * Execute it immediately.
     * @return This object
     */
    TxBuilder synchronously();

    /**
     * Set scope, if necessary.
     * @param bout The bout where this transaction is happening
     * @return This object
     */
    TxBuilder inBout(Bout bout);

    /**
     * Set progress reporter.
     * @param progress Progress reporter
     * @return This object
     */
    TxBuilder progress(TxProgress progress);

    /**
     * Add argument.
     * @param arg The argument
     * @return This object
     */
    TxBuilder arg(Object arg);

    /**
     * Set default value to return.
     * @param value The value
     * @return This object
     */
    TxBuilder asDefault(Object value);

    /**
     * Set preliminary value to return (when transaction is not completed yet).
     * @param value The value
     * @return This object
     */
    TxBuilder asPreliminary(Object value);

    /**
     * Once it's done all other transactions with these mnemos should be
     * removed from cache.
     * @param regex Regular expression to find mnemos of other transactions
     * @return This object
     */
    TxBuilder expire(String regex);

    /**
     * Don't cache the result of execution.
     * @return This object
     */
    TxBuilder noCache();

    /**
     * Execute it and return value.
     * @param <T> Type of response
     * @return The result
     */
    <T> T exec();

}
