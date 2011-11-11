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
package com.netbout.hub;

import com.ymock.util.Logger;
import org.apache.commons.lang.StringUtils;

/**
 * Queue of transactions processed by helpers.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class HelpQueue {

    /**
     * Priority.
     */
    public enum Priority {
        SYNCHRONOUSLY,
        ASAP
    }

    /**
     * Execute one request.
     * @param mnemo Mnemo-code of the request
     * @param type Type of response expected
     * @param priority How important it is (0 means immediately)
     * @param args Optional arguments
     * @return The result
     * @param <T> Type of response
     */
    public static <T> T exec(final String mnemo, final Class<T> type,
        final HelpQueue.Priority priority, final Object... args) {
        final String output = "0";
        Object result;
        if (type.equals(String.class)) {
            result = output;
        } else if (type.equals(Long.class)) {
            result = Long.valueOf(output);
        } else if (type.equals(Boolean.class)) {
            result = !output.isEmpty();
        } else if (type.equals(Long[].class)) {
            final String[] parts = StringUtils.split(output, ',');
            result = new Long[parts.length];
            for (int pos = 0; pos < parts.length; pos += 1) {
                ((Long[]) result)[pos] = Long.valueOf(parts[pos]);
            }
        } else if (type.equals(String[].class)) {
            result = StringUtils.split(output, ';');
        } else {
            throw new IllegalArgumentException(
                String.format(
                    "Result type '%s' is not supported",
                    type.getName()
                )
            );
        }
        Logger.debug(
            HelpQueue.class,
            "#exec(%s, %s, %s, ...): returned %s",
            mnemo,
            type,
            priority,
            result.getClass().getName()
        );
        return (T) result;
    }

}
