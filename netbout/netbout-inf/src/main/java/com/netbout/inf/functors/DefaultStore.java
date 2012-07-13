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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
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
        for (Class<?> type : ref.getSubTypesOf(Functor.class)) {
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
                type.getAnnotation(Functor.NamedAs.class).value(),
                functor
            );
        }
        Logger.debug(
            DefaultStore.class,
            "#discover(): %d functors discovered in classpath: %[list]s",
            map.size(),
            map.keySet()
        );
        final List<String> ordered = DefaultStore.order(map);
        final ConcurrentMap<String, Functor> sorted =
            new ConcurrentSkipListMap<String, Functor>(
                new Comparator<String>() {
                    public int compare(final String left, final String right) {
                        return new Integer(ordered.indexOf(left)).compareTo(
                            new Integer(ordered.indexOf(right))
                        );
                    }
                }
            );
        sorted.putAll(map);
        return sorted;
    }

    /**
     * Topological sorting of the provided map.
     * @param map The map of functors
     * @return Sorted list of their names
     * @see http://en.wikipedia.org/wiki/Topological_sort
     */
    private static List<String> order(
        final ConcurrentMap<String, Functor> map) {
        final ConcurrentMap<String, Collection<String>> deps =
            DefaultStore.deps(map);
        final List<String> ordered = new ArrayList<String>(deps.size());
        while (!deps.isEmpty()) {
            for (String name : deps.keySet()) {
                final Collection<String> parents = deps.get(name);
                for (String parent : parents) {
                    if (!deps.containsKey(parent)) {
                        parents.remove(parent);
                    }
                }
                if (parents.isEmpty()) {
                    deps.remove(name);
                    ordered.add(name);
                }
            }
        }
        return ordered;
    }

    /**
     * Discover dependencies.
     * @param map The map of functors
     * @return Map of names and who they depend on
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private static ConcurrentMap<String, Collection<String>> deps(
        final ConcurrentMap<String, Functor> map) {
        final ConcurrentMap<String, Collection<String>> deps =
            new ConcurrentHashMap<String, Collection<String>>(map.size());
        for (String name : map.keySet()) {
            final Collection<String> parents =
                new ConcurrentSkipListSet<String>();
            deps.put(name, parents);
            final Functor.DependsOn annot = map.get(name).getClass()
                .getAnnotation(Functor.DependsOn.class);
            if (annot != null) {
                for (Class<?> parent : annot.value()) {
                    parents.add(
                        parent.getAnnotation(Functor.NamedAs.class).value()
                    );
                }
            }
        }
        return deps;
    }

}
