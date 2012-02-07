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
package com.netbout.inf;

/**
 * One message to use in predicates.
 *
 * <p>Implementation of this class should be immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public interface Msg {

    /**
     * Show some stats.
     * @return Text stats
     */
    String statistics();

    /**
     * Its number.
     * @return The number
     */
    Long number();

    /**
     * Remove this property.
     * @param name The name of the property to get
     */
    void clear(String name);

    /**
     * Has property with this required value.
     * @param name The name of the property
     * @param value The value required
     * @return Yes, it has this property
     * @param <T> Type of property
     */
    <T> boolean has(String name, T value);

    /**
     * Get property.
     * @param name The name of the property to get
     * @param <T> Type of response
     * @return Value of the property
     */
    <T> T get(String name);

    /**
     * Put property.
     * @param name The name of the property to save
     * @param value The value to set
     * @param <T> Type of property
     */
    <T> void put(String name, T value);

    /**
     * Add property.
     * @param name The name of the property to save
     * @param value The value to set
     * @param <T> Type of property
     */
    <T> void add(String name, T value);

}
