/**
 * Copyright (c) 2009-2011, NetBout.com
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

import java.util.Date;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Mapper of types to String and backwards.
 *
 * <p>These formats are supported:
 *
 * <ul>
 * <li><tt>NULL</tt>
 * <li><tt>true</tt> or <tt>false</tt>
 * <li>plain text in UTF-8 in double quotes
 * <li>integer number (convertable to {@link Long})
 * <li>list of {@link Long} numbers separated by <tt>,</tt> (comma)
 * <li>list of texts in double quotes separated by <tt>,</tt>
 * </ul>
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class TypeMapper {

    /**
     * NULL representation in text.
     */
    private static final String TEXT_NULL = "NULL";

    /**
     * Separator between name and hash.
     */
    private static final String SEPARATOR = ",";

    /**
     * Encoding to be used.
     */
    private static final String ENCODING = "UTF-8";

    /**
     * It's a utility class.
     */
    private TypeMapper() {
        // empty
    }

    /**
     * Convert from given type to string.
     * @param data Data to convert
     * @return The response as explained above
     * @throws HelperException If there is some problem inside
     */
    public static String toText(final Object data) throws HelperException {
        if (data == null) {
            return TypeMapper.TEXT_NULL;
        }
        String result;
        final Class type = data.getClass();
        if (type.equals(String.class)) {
            result = TypeMapper.quote(data.toString());
        } else if (type.equals(Long.class)) {
            result = data.toString();
        } else if (type.equals(Boolean.class)) {
            result = data.toString();
        } else if (type.equals(Date.class)) {
            result = TypeMapper.asText((Date) data);
        } else if (type.equals(Long[].class)) {
            result = TypeMapper.join((Long[]) data);
        } else if (type.equals(String[].class)) {
            final String[] quoted = new String[((String[]) data).length];
            for (int pos = 0; pos < ((String[]) data).length; pos += 1) {
                quoted[pos] = TypeMapper.quote(((String[]) data)[pos]);
            }
            result = TypeMapper.join(quoted);
        } else {
            throw new HelperException(
                "Can't convert '%s' (%s) to String",
                data.toString(),
                type.getName()
            );
        }
        return result;
    }

    /**
     * Convert string to the type.
     * @param text Text to convert
     * @param type Destination type
     * @param <T> The type of response
     * @return The data
     * @throws HelperException If there is some problem inside
     * @checkstyle CyclomaticComplexity (40 lines)
     */
    public static <T> T toObject(final String text, final Class<T> type)
        throws HelperException {
        if (text == TypeMapper.TEXT_NULL) {
            return null;
        }
        Object result;
        if (type.equals(String.class)) {
            result = TypeMapper.unquote(text);
        } else if (type.equals(Long.class)) {
            result = Long.valueOf(text);
        } else if (type.equals(Boolean.class)) {
            result = Boolean.valueOf(text);
        } else if (type.equals(Date.class)) {
            result = TypeMapper.asDate(text);
        } else if (type.equals(Long[].class)) {
            final String[] parts = TypeMapper.split(text);
            result = new Long[parts.length];
            for (int pos = 0; pos < parts.length; pos += 1) {
                ((Long[]) result)[pos] = Long.valueOf(parts[pos]);
            }
        } else if (type.equals(String[].class)) {
            if (text.isEmpty()) {
                result = new String[0];
            } else {
                final String[] parts = TypeMapper.split(text);
                result = new String[parts.length];
                for (int pos = 0; pos < parts.length; pos += 1) {
                    ((String[]) result)[pos] = TypeMapper.unquote(parts[pos]);
                }
            }
        } else {
            throw new HelperException(
                "Can't convert '%s' to unsupported type %s",
                text,
                type.getName()
            );
        }
        return (T) result;
    }

    /**
     * Quote the text.
     * @param text The text to quote
     * @return The text safely quoted/encoded
     */
    private static String quote(final String text) {
        try {
            return new Base64().encodeToString(
                text.getBytes(TypeMapper.ENCODING)
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
    private static String unquote(final String text) {
        try {
            return new String(new Base64().decode(text), TypeMapper.ENCODING);
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Join group of objects.
     * @param group The group of them
     * @return Text
     */
    private static String join(final Object[] group) {
        return StringUtils.join(group, TypeMapper.SEPARATOR);
    }

    /**
     * Split text to group of objects.
     * @param text The text
     * @return Group
     */
    private static String[] split(final String text) {
        return StringUtils.split(text, TypeMapper.SEPARATOR);
    }

    /**
     * Convert date to text.
     * @param date The date to convert
     * @return The text
     */
    private static String asText(final Date date) {
        return TypeMapper.dateFormatter().print(date.getTime());
    }

    /**
     * Convert text to date.
     * @param text The text to convert
     * @return The date
     */
    private static Date asDate(final String text) {
        return new Date(TypeMapper.dateFormatter().parseMillis(text));
    }

    /**
     * Create and return date time formatter.
     * @return The formatter
     */
    private static DateTimeFormatter dateFormatter() {
        return ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);
    }

}
