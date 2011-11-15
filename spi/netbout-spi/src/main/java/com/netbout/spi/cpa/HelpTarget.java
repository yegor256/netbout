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

import com.netbout.spi.HelperException;
import com.netbout.spi.TypeMapper;
import java.lang.reflect.Method;

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
        return TypeMapper.toText(result);
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
        final Object[] converted = new Object[args.length];
        for (int pos = 0; pos < args.length; pos += 1) {
            converted[pos] = TypeMapper.toObject(args[pos], types[pos]);
        }
        return converted;
    }

}
