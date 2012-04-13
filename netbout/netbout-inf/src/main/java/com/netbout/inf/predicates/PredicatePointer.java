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
package com.netbout.inf.predicates;

import com.netbout.inf.Atom;
import com.netbout.inf.Notice;
import com.netbout.inf.Pointer;
import com.netbout.inf.Predicate;
import com.netbout.inf.PredicateException;
import com.netbout.spi.Message;
import com.ymock.util.Logger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.reflections.Reflections;

/**
 * Pointer to predicate.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class PredicatePointer implements Pointer {

    /**
     * The type.
     */
    private final transient Class<? extends Predicate> type;

    /**
     * Meta info.
     */
    private final transient Meta meta;

    /**
     * Public ctor.
     * @param pred The class just found
     */
    public PredicatePointer(final Class<? extends Predicate> pred) {
        this.type = pred;
        this.meta = pred.getAnnotation(Meta.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String statistics() {
        return this.type.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.type.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        // nothing to close here
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean pointsTo(final String name) {
        return this.meta.value().equals(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate build(final String name, final List<Atom> atoms) {
        if (!name.equals(this.meta.value())) {
            throw new PredicateException("illegal name");
        }
        Predicate predicate;
        try {
            if (this.type.getConstructors().length > 0) {
                predicate = (Predicate) this.type
                    .getConstructor(List.class)
                    .newInstance(atoms);
            } else {
                predicate = (Predicate) this.type.newInstance();
            }
        } catch (NoSuchMethodException ex) {
            throw new PredicateException(ex);
        } catch (InstantiationException ex) {
            throw new PredicateException(ex);
        } catch (IllegalAccessException ex) {
            throw new PredicateException(ex);
        } catch (java.lang.reflect.InvocationTargetException ex) {
            throw new PredicateException(ex);
        }
        return predicate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void see(final Notice notice) {
        // nothing to do here
    }

    /**
     * Discover all predicates.
     * @return List of pointers to predicates
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public static Set<Pointer> discover() {
        final Reflections ref = new Reflections(
            PredicatePointer.class.getPackage().getName()
        );
        final Set<Pointer> ptrs = new HashSet<Pointer>();
        for (Class pred : ref.getTypesAnnotatedWith(Meta.class)) {
            ptrs.add(new PredicatePointer(pred));
        }
        Logger.debug(
            PredicatePointer.class,
            "#discover(): %d predicates discovered in classpath: %[list]s",
            ptrs.size(),
            ptrs
        );
        return ptrs;
    }

}
