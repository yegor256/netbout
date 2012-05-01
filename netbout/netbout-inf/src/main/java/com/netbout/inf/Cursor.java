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
package com.netbout.inf;

/**
 * Cursor in {@link Ray}.
 *
 * <p>Implementation must be immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public interface Cursor {

    /**
     * Add attribute to every msg including this one, which satisfy the
     * term.
     * @param term The term to satisfy
     * @param attr The attr of attribute to set
     * @param value The value to set
     */
    void add(Term term, String attr, String value);

    /**
     * Replace attribute to every msg including this one, which satisfy the
     * term.
     * @param term The term to satisfy
     * @param attr The attr of attribute to set
     * @param value The value to set
     */
    void replace(Term term, String attr, String value);

    /**
     * Delete attribute from every msg including this one, which satisfy the
     * term.
     * @param term The term to satisfy
     * @param attr The attr of attribute to delete
     */
    void delete(Term term, String attr);

    /**
     * Delete attribute from every msg including this one, which satisfy the
     * term.
     * @param term The term to satisfy
     * @param attr The attr of attribute to delete
     * @param value The value to delete
     */
    void delete(Term term, String attr, String value);

    /**
     * Shift cursor to the next message, which satisfies the term.
     * @param term The term to satisfy
     * @return New cursor
     */
    Cursor shift(Term term);

    /**
     * Make a copy.
     * @return New cursor
     */
    Cursor copy();

    /**
     * Get message (throws runtime exception if there is no message).
     * @return The message
     */
    Msg msg();

    /**
     * Is it the end of ray?
     * @return TRUE if this is the end
     */
    boolean end();

}
