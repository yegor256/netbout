/**
 * Copyright (c) 2009-2012, Netbout.com
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
package com.netbout.inf.functors;

import com.jcabi.log.Logger;
import com.netbout.inf.Functor;
import com.netbout.inf.InvalidSyntaxException;
import com.netbout.inf.Notice;
import com.netbout.inf.Ray;
import com.netbout.inf.Store;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.reflections.Reflections;

/**
 * Store of all known functors.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class DefaultStore implements Store {

    /**
     * All known functors.
     */
    private final transient ConcurrentMap<String, Functor> functors =
        DefaultStore.discover();

    /**
     * {@inheritDoc}
     */
    @Override
    public void see(final Ray ray, final Notice notice) {
        for (Functor functor : this.functors.values()) {
            for (Method method : functor.getClass().getMethods()) {
                if (method.getAnnotation(Noticable.class) == null) {
                    continue;
                }
                if (!method.getParameterTypes()[1].isInstance(notice)) {
                    continue;
                }
                try {
                    method.invoke(
                        functor,
                        ray,
                        method.getParameterTypes()[1].cast(notice)
                    );
                } catch (IllegalAccessException ex) {
                    throw new IllegalStateException(method.toString(), ex);
                } catch (java.lang.reflect.InvocationTargetException ex) {
                    throw new IllegalStateException(method.toString(), ex);
                }
                Logger.debug(
                    this,
                    "#see(): %s received %[type]s",
                    method.toGenericString(),
                    notice
                );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Functor get(final String name) throws InvalidSyntaxException {
        final Functor functor = this.functors.get(name);
        if (functor == null) {
            throw new InvalidSyntaxException(
                String.format("Unknown functor '%s'", name)
            );
        }
        return functor;
    }

    /**
     * Discover all predicates.
     * @return List of pointers to predicates
     */
    private static ConcurrentMap<String, Functor> discover() {
        final Reflections ref = new Reflections(
            DefaultStore.class.getPackage().getName()
        );
        final ConcurrentMap<String, Functor> map =
            new ConcurrentHashMap<String, Functor>();
        for (Class type : ref.getSubTypesOf(Functor.class)) {
            if (type.isMemberClass() || type.isAnonymousClass()) {
                continue;
            }
            Functor functor;
            try {
                functor = Functor.class.cast(type.newInstance());
            } catch (InstantiationException ex) {
                throw new IllegalStateException(ex);
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            }
            map.put(
                NamedAs.class.cast(type.getAnnotation(NamedAs.class)).value(),
                functor
            );
        }
        Logger.debug(
            DefaultStore.class,
            "#discover(): %d functors discovered in classpath: %[list]s",
            map.size(),
            map.keySet()
        );
        return map;
    }

}
