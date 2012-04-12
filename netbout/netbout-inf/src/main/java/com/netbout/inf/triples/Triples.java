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
package com.netbout.inf.triples;

import java.io.Closeable;
import java.util.Iterator;

/**
 * Triples.
 *
 * <p>Implementation must be thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public interface Triples extends Closeable {

    /**
     * Put new triple.
     * @param number The number
     * @param name Name of triple
     * @param value The value
     */
    void put(Long number, String name, String value);

    /**
     * Does it have this triple?
     * @param number The number
     * @param name Name of triple
     * @param value The value
     * @return Yes or no
     */
    boolean has(Long number, String name, String value);

    /**
     * Get the value (the first one).
     * @param number The number
     * @param name Name of triple
     * @return The value found
     * @throws MissedTripleException If not found
     */
    String get(Long number, String name) throws MissedTripleException;

    /**
     * Get all triples.
     * @param number The number
     * @param name Name of triple
     * @return The values found
     */
    Iterator<String> all(Long number, String name);

    /**
     * Reverse lookup.
     * @param name Name of triple
     * @param value The value to look for
     * @return Reverse sorted list of numbers found
     */
    Iterator<Long> reverse(String name, String value);

    /**
     * Reverse lookup, by any of the values provided.
     * @param name Name of triple
     * @param join Name of joining triple
     * @param value The values to look for
     * @return Reverse sorted list of numbers found
     */
    Iterator<Long> reverse(String name, String join, String value);

    /**
     * Clear these triples.
     * @param number The number
     * @param name Name of triple
     */
    void clear(Long number, String name);

}
