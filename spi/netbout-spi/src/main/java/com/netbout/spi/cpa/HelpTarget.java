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
package com.netbout.spi.cpa;

import com.netbout.spi.Helper;
import com.netbout.spi.HelperException;
import com.ymock.util.Logger;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.reflections.Reflections;

/**
 * Classpath annotations helper.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class HelpTarget {

    /**
     * The farm.
     */
    private final Object farm;

    /**
     * The method to call.
     */
    private final Method method;

    /**
     * Public ctor.
     * @param frm Farm object
     * @param mtd Method to call on this farm
     */
    public HelpTarget(final Object frm, final Method mtd) {
        this.farm = frm;
        this.method = mtd;
    }

    /**
     * Execute it with arguments.
     * @param args Arguments
     * @return The response
     * @throws HelperException If some problem inside
     */
    public String execute(final String[] args) throws HelperException {
        Object result;
        try {
            result = this.method.invoke(
                this.farm,
                this.converted(args, this.method.getParameterTypes())
            );
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        } catch (java.lang.reflect.InvocationTargetException ex) {
            throw new IllegalStateException(ex);
        }
        return this.revert(result);
    }

    /**
     * Convert argument types.
     * @param args Arguments
     * @param types Expected types for every one of them
     * @return Array of properly types args
     * @throws HelperException If some problem inside
     */
    public Object[] converted(final String[] args, final Class[] types)
        throws HelperException {
        if (types.length != args.length) {
            throw new HelperException(
                "Method %s expects %d args while only %d provided",
                this.method.toGenericString(),
                types.length,
                args.length
            );
        }
        Object[] converted = new Object[args.length];
        for (int pos = 0; pos < args.length; pos += 1) {
            converted[pos] = this.convert(args[pos], types[pos]);
        }
        return converted;
    }

    /**
     * Convert one value to the type.
     * @param value The value to convert
     * @param type Expected type
     * @return New value of new type
     * @param <T> Type of response expected
     * @throws HelperException If some problem inside
     */
    public <T> T convert(final String value, final Class<T> type)
        throws HelperException {
        Object ready;
        if (type.equals(String.class)) {
            ready = value;
        } else if (type.equals(Long.class)) {
            ready = Long.valueOf(value);
        } else if (type.equals(Boolean.class)) {
            ready = Boolean.valueOf(value);
        } else {
            throw new HelperException(
                "Can't convert '%s' to unsupported type %s",
                value,
                type.getName()
            );
        }
        return (T) ready;
    }

    /**
     * Convert one value to string.
     * @param value The value to convert
     * @return String representation of it
     * @throws HelperException If some problem inside
     */
    public String revert(final Object value) throws HelperException {
        if (value == null) {
            return "NULL";
        }
        String ready;
        final Class type = value.getClass();
        if (type.equals(String.class)) {
            ready = String.format("\"%s\"", value.toString());
        } else if (type.equals(Long.class)) {
            ready = value.toString();
        } else if (type.equals(Integer.class)) {
            ready = value.toString();
        } else if (type.equals(Boolean.class)) {
            if ((Boolean) value) {
                ready = "1";
            } else {
                ready = "0";
            }
        } else if (type.equals(Long[].class)) {
            ready = StringUtils.join((Long[]) value, ",");
        } else if (type.equals(String[].class)) {
            String[] quoted = new String[((String[]) value).length];
            for (int pos = 0; pos < ((String[]) value).length; pos += 1) {
                quoted[pos] = ((String[]) value)[pos].replace("\"", "\\\"");
            }
            ready = String.format(
                "\"%s\"",
                StringUtils.join(quoted, "\",\"")
            );
        } else {
            throw new HelperException(
                "Can't revert %s to String",
                type.getName()
            );
        }
        return ready;
    }

}
