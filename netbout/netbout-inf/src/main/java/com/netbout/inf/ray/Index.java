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
package com.netbout.inf.ray;

import java.util.Set;
import java.util.SortedSet;

/**
 * Index, as a map of String to SortedSet of Long.
 *
 * <p>Implementation must be thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
interface Index {

    /**
     * Replace all existing values for this number with this one.
     * @param msg Number of message
     * @param value The value to set
     */
    void replace(long msg, String value);

    /**
     * Add this value to the message.
     * @param msg Number of message
     * @param value The value to add
     */
    void add(long msg, String value);

    /**
     * Delete this value from the message.
     * @param msg Number of message
     * @param value The value to delete
     */
    void delete(long msg, String value);

    /**
     * Delete all values from the message.
     * @param msg Number of message
     */
    void clean(long msg);

    /**
     * Return first value of this message.
     * @param msg Number of message
     * @return The value
     */
    String first(long msg);

    /**
     * Get sorted set of numbers for the given value.
     * @param value The value
     * @return Set of message numbers
     */
    SortedSet<Long> msgs(String value);

    /**
     * All values.
     * @return The values
     */
    Set<String> values();

}
