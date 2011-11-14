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
import org.apache.commons.lang.StringUtils;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Mapper of types to String and backwards.
 *
 * <p>These formats are supported:
 *
 * <ul>
 * <li><tt>NULL</tt>
 * <li>plain text in UTF-8 in double quotes
 * <li>integer number (convertable to {@link Long})
 * <li>list of {@link Long} numbers separated by <tt>,</tt> (comma)
 * <li>list of texts in double quotes separated by <tt>,</tt> (double quote
 * inside the text should be escaped by a backslash)
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
     * Convert from given type to string.
     * @param data Data to convert
     * @return The response as explained above
     * @throws HelperException If there is some problem inside
     */
    public static String toText(Object data) throws HelperException {
        if (data == null) {
            return TypeMapper.TEXT_NULL;
        }
        String result;
        final Class type = data.getClass();
        if (type.equals(String.class)) {
            result = String.format("\"%s\"", data.toString());
        } else if (type.equals(Long.class)) {
            result = data.toString();
        } else if (type.equals(Integer.class)) {
            result = data.toString();
        } else if (type.equals(Boolean.class)) {
            if ((Boolean) data) {
                result = "1";
            } else {
                result = "0";
            }
        } else if (type.equals(Date.class)) {
            result = TypeMapper.asText((Date) data);
        } else if (type.equals(Long[].class)) {
            result = StringUtils.join((Long[]) data, ",");
        } else if (type.equals(String[].class)) {
            String[] quoted = new String[((String[]) data).length];
            for (int pos = 0; pos < ((String[]) data).length; pos += 1) {
                quoted[pos] = ((String[]) data)[pos].replace("\"", "\\\"");
            }
            result = String.format(
                "\"%s\"",
                StringUtils.join(quoted, "\",\"")
            );
        } else {
            throw new HelperException(
                "Can't convert %s to String",
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
     */
    public static <T> T toObject(String text, Class<T> type)
        throws HelperException {
        if (text == TypeMapper.TEXT_NULL) {
            return null;
        }
        Object result;
        if (type.equals(String.class)) {
            result = text.substring(1, text.length() - 1);
        } else if (type.equals(Long.class)) {
            result = Long.valueOf(text);
        } else if (type.equals(Boolean.class)) {
            result = text != "0";
        } else if (type.equals(Date.class)) {
            result = TypeMapper.asDate(text);
        } else if (type.equals(Long[].class)) {
            final String[] parts = StringUtils.split(text, ',');
            result = new Long[parts.length];
            for (int pos = 0; pos < parts.length; pos += 1) {
                ((Long[]) result)[pos] = Long.valueOf(parts[pos]);
            }
        } else if (type.equals(String[].class)) {
            if (text.isEmpty()) {
                result = new String[0];
            } else {
                final String[] parts = StringUtils.split(
                    text.substring(1, text.length() - 1),
                    "\",\""
                );
                result = new String[parts.length];
                for (int pos = 0; pos < parts.length; pos += 1) {
                    ((String[]) result)[pos] = parts[pos];
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
     * Convert date to text.
     * @param date The date to convert
     * @return The text
     */
    private static String asText(final Date date) {
        return ISODateTimeFormat.dateTime().print(date.getTime());
    }

    /**
     * Convert text to date.
     * @param text The text to convert
     * @return The date
     */
    private static Date asDate(final String text) {
        return new Date(ISODateTimeFormat.dateTime().parseMillis(text));
    }

}
