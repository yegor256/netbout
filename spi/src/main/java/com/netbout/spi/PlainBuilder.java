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
package com.netbout.spi;

import com.netbout.spi.plain.PlainBoolean;
import com.netbout.spi.plain.PlainDate;
import com.netbout.spi.plain.PlainList;
import com.netbout.spi.plain.PlainLong;
import com.netbout.spi.plain.PlainString;
import com.netbout.spi.plain.PlainURL;
import com.netbout.spi.plain.PlainUrn;
import com.ymock.util.Logger;
import java.net.URL;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Plain type builder.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public final class PlainBuilder {

    /**
     * It's a utility class.
     */
    private PlainBuilder() {
        // empty
    }

    /**
     * Convert an object to a {@link Plain} value type.
     * @param data Object to convert
     * @return The plain object
     * @param <T> Type to return
     */
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public static <T> Plain<T> fromObject(final Object data) {
        if (data == null) {
            throw new IllegalArgumentException("Can't convert NULL");
        }
        Plain<T> result;
        if (data instanceof String) {
            result = (Plain) new PlainString(data.toString());
        } else if (data instanceof Long) {
            result = (Plain) new PlainLong((Long) data);
        } else if (data instanceof Boolean) {
            result = (Plain) new PlainBoolean((Boolean) data);
        } else if (data instanceof Date) {
            result = (Plain) new PlainDate((Date) data);
        } else if (data instanceof URL) {
            result = (Plain) new PlainURL((URL) data);
        } else if (data instanceof Urn) {
            result = (Plain) new PlainUrn((Urn) data);
        } else if (data instanceof List) {
            result = (Plain) new PlainList((List) data);
        } else {
            throw new IllegalArgumentException(
                Logger.format(
                    "Can't convert '%s' (%[type]s) to Plain<?>",
                    StringEscapeUtils.escapeJava(data.toString()),
                    data
                )
            );
        }
        return result;
    }

    /**
     * Convert a text to a {@link Plain} value type.
     * @param text Text to parse
     * @return The plain object
     * @param <T> Type to return
     */
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public static <T> Plain<T> fromText(final String text) {
        if (text == null) {
            throw new IllegalArgumentException("Can't convert NULL as text");
        }
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Empty string can't have value");
        }
        Plain<T> result;
        if (PlainList.isIt(text)) {
            result = (Plain) PlainList.valueOf(text);
        } else if (PlainUrn.isIt(text)) {
            result = (Plain) new PlainUrn(text);
        } else if (PlainURL.isIt(text)) {
            result = (Plain) new PlainURL(text);
        } else if (PlainDate.isIt(text)) {
            result = (Plain) new PlainDate(text);
        } else if (PlainLong.isIt(text)) {
            result = (Plain) new PlainLong(text);
        } else if (PlainBoolean.isIt(text)) {
            result = (Plain) new PlainBoolean(text);
        } else if (PlainString.isIt(text)) {
            result = (Plain) PlainString.valueOf(text);
        } else {
            throw new IllegalArgumentException(
                Logger.format(
                    "Can't convert text '%s' to Plain<?>",
                    StringEscapeUtils.escapeJava(text)
                )
            );
        }
        return result;
    }

}
