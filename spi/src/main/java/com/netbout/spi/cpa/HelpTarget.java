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
package com.netbout.spi.cpa;

import com.jcabi.log.Logger;
import com.netbout.spi.PlainBuilder;
import com.netbout.spi.Token;
import com.netbout.spi.plain.PlainVoid;
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
    private final transient Object farm;

    /**
     * The method to call.
     */
    private final transient Method method;

    /**
     * Private ctor, use {@link #build(Object,Method)} instead.
     * @param frm Farm object
     * @param mtd Method to call on this farm
     */
    private HelpTarget(final Object frm, final Method mtd) {
        this.farm = frm;
        this.method = mtd;
    }

    /**
     * Build new object.
     * @param frm Farm object
     * @param mtd Method to call on this farm
     * @return The target created
     */
    public static HelpTarget build(final Object frm, final Method mtd) {
        return new HelpTarget(frm, mtd);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.method.toGenericString();
    }

    /**
     * Execute it with arguments.
     * @param token The token
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public void execute(final Token token) {
        final Object[] params = this.converted(
            token,
            this.method.getParameterTypes()
        );
        Object result;
        try {
            result = this.method.invoke(this.farm, params);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(
                String.format(
                    "Failed to access \"%s\"",
                    this.method.toGenericString()
                ),
                ex
            );
        } catch (java.lang.reflect.InvocationTargetException ex) {
            throw new IllegalStateException(
                Logger.format(
                    "Failed to call \"%s\" with %[list]s",
                    this.method.toGenericString(),
                    this.typesOf(params)
                ),
                ex
            );
        // @checkstyle IllegalCatch (1 line)
        } catch (Throwable ex) {
            throw new IllegalArgumentException(
                Logger.format(
                    "Exception in \"%s\" with %[list]s",
                    this.method.toGenericString(),
                    this.typesOf(params)
                ),
                ex
            );
        }
        if (this.method.getReturnType().equals(Void.TYPE)) {
            token.result(new PlainVoid());
        } else if (result != null) {
            token.result(PlainBuilder.fromObject(result));
        }
    }

    /**
     * Convert argument types.
     * @param token The token
     * @param types Expected types for every one of them
     * @return Array of properly typed args
     */
    private static Object[] converted(final Token token, final Class[] types) {
        final Object[] converted = new Object[types.length];
        for (int pos = 0; pos < types.length; pos += 1) {
            converted[pos] = token.arg(pos).value();
        }
        return converted;
    }

    /**
     * Extract types of objects into array.
     * @param objects Objects
     * @return Array of their types
     */
    private static Class[] typesOf(final Object[] objects) {
        final Class[] types = new Class[objects.length];
        for (int pos = 0; pos < objects.length; pos += 1) {
            types[pos] = objects[pos].getClass();
        }
        return types;
    }

}
