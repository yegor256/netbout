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
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.CharEncoding;

/**
 * Plain string.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class PlainString implements Plain<String> {

    /**
     * Quote around a string.
     */
    private static final String QUOTE = "\"";

    /**
     * The value.
     */
    private final transient String ivalue;

    /**
     * Public ctor.
     * @param text The text presentation
     */
    public PlainString(final String text) {
        this.ivalue = text;
    }

    /**
     * Is it of our type?
     * @param text The text
     * @return Is it or not?
     */
    public static boolean isIt(final String text) {
        return text.startsWith(PlainString.QUOTE)
            && text.endsWith(PlainString.QUOTE);
    }

    /**
     * From text.
     * @param text The text presentation
     * @return The object
     */
    public static PlainString valueOf(final String text) {
        if (text.length() < PlainString.QUOTE.length() * 2) {
            throw new IllegalArgumentException(
                String.format(
                    "Text '%s' is too short for decoding",
                    text
                )
            );
        }
        return new PlainString(
            PlainString.decode(
                text.substring(
                    PlainString.QUOTE.length(),
                    text.length() - PlainString.QUOTE.length()
                )
            )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.ivalue.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return (obj instanceof PlainString)
            && (this.hashCode() == obj.hashCode());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value() {
        return this.ivalue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("%s%s%1$s", this.QUOTE, this.encode(this.ivalue));
    }

    /**
     * Quote the text.
     * @param text The text to quote
     * @return The text safely quoted/encoded
     */
    private static String encode(final String text) {
        try {
            return new Base64().encodeToString(
                text.getBytes(CharEncoding.UTF_8)
            );
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Un-quote the text.
     * @param text The text safely quoted/encoded
     * @return Normal text
     */
    private static String decode(final String text) {
        try {
            return new String(new Base64().decode(text), CharEncoding.UTF_8);
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

}
