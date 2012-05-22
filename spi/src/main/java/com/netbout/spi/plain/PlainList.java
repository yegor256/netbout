/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi.plain;

import com.netbout.spi.Plain;
import com.netbout.spi.PlainBuilder;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 * Plain list.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class PlainList<T> implements Plain<List<T>> {

    /**
     * Separator between values.
     */
    private static final String SEPARATOR = ";";

    /**
     * The value.
     */
    private final transient List<T> list;

    /**
     * Public ctor.
     * @param val The value
     */
    public PlainList(final List<T> val) {
        this.list = val;
    }

    /**
     * Is it of our type?
     * @param text The text
     * @return Is it or not?
     */
    public static boolean isIt(final String text) {
        return !text.isEmpty() && text.charAt(0) == '[';
    }

    /**
     * Retrive value from text.
     * @param text The text
     * @return Is it or not?
     * @param <T> Type to return
     */
    @SuppressWarnings("unchecked")
    public static <T> PlainList<T> valueOf(final String text) {
        final List<T> parts = new ArrayList<T>();
        for (String element : PlainList.unpack(text)) {
            parts.add((T) PlainBuilder.fromText(element).value());
        }
        return new PlainList(parts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.list.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return obj == this || ((obj instanceof PlainList)
            && (this.hashCode() == obj.hashCode()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> value() {
        return this.list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final List<String> parts = new ArrayList<String>();
        for (T element : this.list) {
            parts.add(PlainBuilder.fromObject(element).toString());
        }
        return this.pack(parts);
    }

    /**
     * Join group of objects.
     * @param group The group of them
     * @return Text
     */
    public static String pack(final List<String> group) {
        return String.format(
            "[%s]",
            StringUtils.join(group, PlainList.SEPARATOR)
        );
    }

    /**
     * Split text to group of objects.
     * @param text The text
     * @return Group
     */
    public static String[] unpack(final String text) {
        return StringUtils.split(
            StringUtils.substring(text, 1, -1),
            PlainList.SEPARATOR
        );
    }

}
