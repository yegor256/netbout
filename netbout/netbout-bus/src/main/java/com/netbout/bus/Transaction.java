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
import com.netbout.spi.Plain;
import java.util.regex.Pattern;

/**
 * One transaction.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
interface Transaction {

    /**
     * Make a token out of it.
     * @return The token
     */
    TxToken makeToken();

    /**
     * Get default result to return.
     * @return The result
     */
    Plain<?> getDefaultResult();

    /**
     * Is it inside bout?
     * @return Yes or no
     */
    boolean isInsideBout();

    /**
     * Get bout where it should be executed (or exception if it's global).
     * @return Bout
     */
    Bout getBout();

    /**
     * Should we cache its results?
     * @return Yes or no
     */
    boolean isCacheEnabled();

        /**
     * Should this transaction "expire" others after its execution?
     * @return Yes or no
     */
    boolean hasToExpireOthers();

    /**
     * Get pattern for expire.
     * @return The pattern
     */
    Pattern getExpirePattern();

}
