/**
 * Copyright (c) 2009-2014, Netbout.com
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
package com.netbout.spi;

import com.jcabi.aspects.Immutable;
import java.io.IOException;

/**
 * Alias.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 2.0
 */
@Immutable
public interface Inbox extends Pageable<Bout> {

    /**
     * Start new bout.
     * @return Bout number
     * @throws IOException If fails
     */
    long start() throws IOException;

    /**
     * Get bout by its number.
     * @param number Bout number
     * @return Bout found
     * @throws Inbox.BoutNotFoundException If not found
     */
    Bout bout(long number) throws Inbox.BoutNotFoundException;

    /**
     * Thowable when bout is not found.
     * @see Inbox#bout(long)
     */
    class BoutNotFoundException extends Exception {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = 0x7526FA78EED21470L;
        /**
         * Public ctor.
         * @param num The number of bout not found
         */
        public BoutNotFoundException(final long num) {
            super(String.format("Bout #%d not found", num));
        }
        /**
         * Public ctor.
         * @param num The number of bout not found
         * @param cause Cause of it
         */
        public BoutNotFoundException(final long num, final Throwable cause) {
            super(String.format("bout #%d not found", num), cause);
        }
    }
}
