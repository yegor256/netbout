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
import com.netbout.spi.OperationFailureException;
import com.ymock.util.Logger;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.reflections.Reflections;

/**
 * Classpath annotations helper.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class CpaHelper implements Helper {

    /**
     * All discovered operations.
     */
    private final Map<String, Target> ops = new HashMap<String, Target>();

    /**
     * Public ctor.
     * @param pkg Name of the package where to look for annotated methods
     *  and farms
     */
    public CpaHelper(final String pkg) {
        final Reflections reflections = new Reflections(pkg);
        for (Class tfarm : reflections.getTypesAnnotatedWith(Farm.class)) {
            Logger.info(
                this,
                "#CpaHelper(%s): @Farm found at '%s'",
                pkg,
                tfarm.getName()
            );
            Object farm;
            try {
                farm = tfarm.newInstance();
            } catch (InstantiationException ex) {
                throw new IllegalArgumentException(ex);
            } catch (IllegalAccessException ex) {
                throw new IllegalArgumentException(ex);
            }
            for (Method method : tfarm.getDeclaredMethods()) {
                final Annotation atn = method.getAnnotation(Operation.class);
                if (atn == null) {
                    continue;
                }
                final String mnemo = ((Operation) atn).value();
                this.ops.put(mnemo, new Target(farm, method));
                Logger.info(
                    this,
                    "#CpaHelper(%s): @Operation('%s') found",
                    pkg,
                    mnemo
                );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> supports() {
        return this.ops.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T execute(final String mnemo, final Class<T> type,
        final Object... args) throws OperationFailureException {
        if (!this.ops.containsKey(mnemo)) {
            throw new IllegalArgumentException("Operation not supported");
        }
        final Object result = this.ops.get(mnemo).execute(args);
        Logger.info(
            this,
            "#execute(%s, %s, %d args): done with %s as a result",
            mnemo,
            type.getName(),
            args.length,
            result.getClass().getName()
        );
        return (T) result;
    }

    /**
     * Connection between farm and operation.
     */
    private static final class Target {
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
        public Target(final Object frm, final Method mtd) {
            this.farm = frm;
            this.method = mtd;
        }
        /**
         * Execute it with arguments.
         * @param args Arguments
         * @return The response
         * @throws OperationFailureException If some problem inside
         */
        public Object execute(final Object[] args)
            throws OperationFailureException {
            try {
                return this.method.invoke(this.farm, args);
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            } catch (java.lang.reflect.InvocationTargetException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

}
