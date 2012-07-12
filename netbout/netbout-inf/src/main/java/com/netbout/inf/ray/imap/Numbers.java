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
package com.netbout.inf.ray.imap;

import com.netbout.inf.Lattice;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Sorted set of message numbers.
 *
 * <p>Implementation must be thread-safe, except {@link #load(InputStream)}
 * and {@link #save(OutputStream)} methods.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public interface Numbers {

    /**
     * Size of one element in input/output stream (size of LONG).
     */
    int SIZE = 8;

    /**
     * How many bytes we consume in memory.
     * @return Number of bytes
     */
    long sizeof();

    /**
     * Get lattice for this collection of numbers.
     * @return The lattice
     */
    Lattice lattice();

    /**
     * Get next number after the provided one, or ZERO.
     * @param number The number to start searching from
     * @return The number, next to this one (or ZERO if nothing found)
     */
    long next(long number);

    /**
     * Add new number (or replace the existing one, silently).
     * @param number The number to add
     */
    void add(long number);

    /**
     * Remove this number (or ignore the operation if it doesn't exist,
     * silently).
     * @param number The number to remove
     */
    void remove(long number);

    /**
     * Save them all to the output stream.
     * @param stream The stream to save to
     * @return How many bytes were just saved
     * @throws IOException If some I/O problem inside
     */
    long save(OutputStream stream) throws IOException;

    /**
     * Load from the input stream and add here.
     * @param stream The stream to load from
     * @throws IOException If some I/O problem inside
     */
    void load(InputStream stream) throws IOException;

}
