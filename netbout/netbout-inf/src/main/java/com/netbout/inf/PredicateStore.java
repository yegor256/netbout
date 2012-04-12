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
package com.netbout.inf;

import com.netbout.inf.ebs.EbsVolume;
import com.netbout.inf.motors.StoreAware;
import com.netbout.inf.predicates.PredicatePointer;
import com.netbout.inf.triples.HsqlTriples;
import com.netbout.inf.triples.Triples;
import com.netbout.spi.Message;
import com.ymock.util.Logger;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.io.IOUtils;
import org.reflections.Reflections;

/**
 * Store of all known predicates.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class PredicateStore implements Store {

    /**
     * Message to some void constant (name of triple).
     */
    private static final String MSG_TO_VOID = "message-to-void";

    /**
     * The folder to work with.
     */
    private final transient Folder folder = new EbsVolume();

    /**
     * Pointers to all known predicates.
     */
    private final transient Set<Pointer> pointers;

    /**
     * Counter of messages indexed.
     */
    private final transient Triples counter;

    /**
     * Maximum successfully indexed number.
     */
    private final transient AtomicLong max = new AtomicLong(0L);

    /**
     * Numbers just done.
     */
    private final transient SortedSet<Long> done =
        new ConcurrentSkipListSet<Long>();

    /**
     * Public ctor.
     */
    public PredicateStore() {
        this.pointers = this.discover();
        this.counter = new HsqlTriples(new File(this.folder.path(), "counter"));
        final Iterator<Long> numbers = this.counter
            .reverse(PredicateStore.MSG_TO_VOID, "");
        if (numbers.hasNext()) {
            this.max.set(numbers.next());
        } else {
            this.max.set(0L);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String statistics() {
        final StringBuilder text = new StringBuilder();
        text.append("Folder stats:\n")
            .append(this.folder.statistics())
            .append("\nStore(s) stats:");
        for (Pointer pointer : this.pointers) {
            text.append("\n").append(pointer.statistics());
        }
        return text.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        for (Pointer pointer : this.pointers) {
            IOUtils.closeQuietly(pointer);
        }
        IOUtils.closeQuietly(this.counter);
        IOUtils.closeQuietly(this.folder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long maximum() {
        while (!this.done.isEmpty()) {
            final Long smallest = this.done.first();
            if (smallest != this.max.get() + 1) {
                break;
            }
            this.max.incrementAndGet();
            this.done.remove(smallest);
            // @checkstyle MagicNumber (1 line)
            if (this.max.get() % 1000 == 0) {
                Logger.info(this, "#maximum(): %d message(s)", this.max.get());
            }
        }
        return this.max.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void see(final Message msg) {
        for (Pointer pointer : this.pointers) {
            pointer.see(msg);
        }
        this.counter.put(this.maximum(), PredicateStore.MSG_TO_VOID, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate build(final String name, final List<Atom> atoms) {
        Predicate predicate = null;
        for (Pointer ptr : this.pointers) {
            if (ptr.pointsTo(name)) {
                predicate = ptr.build(name, atoms);
                break;
            }
        }
        if (predicate == null) {
            throw new PredicateException(
                String.format("Unknown predicate name '%s'", name)
            );
        }
        return predicate;
    }

    /**
     * Discover all predicates.
     * @return List of pointers to predicates
     */
    private Set<Pointer> discover() {
        final Set<Pointer> ptrs = new HashSet<Pointer>();
        ptrs.addAll(PredicatePointer.discover());
        ptrs.addAll(this.motors());
        Logger.info(
            this,
            "#discover(): %d pointer(s) discovered in classpath: %[list]s",
            ptrs.size(),
            ptrs
        );
        return ptrs;
    }

    /**
     * Discover all motors.
     * @return List of pointers to predicates
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Set<Pointer> motors() {
        final Reflections ref = new Reflections(
            StoreAware.class.getPackage().getName()
        );
        final Set<Pointer> motors = new HashSet<Pointer>();
        for (Class pred : ref.getSubTypesOf(Pointer.class)) {
            final File dir = new File(this.folder.path(), pred.getName());
            dir.mkdirs();
            Pointer motor;
            try {
                motor = (Pointer) pred
                    .getConstructor(File.class)
                    .newInstance(dir);
            } catch (NoSuchMethodException ex) {
                throw new PredicateException(pred.getName(), ex);
            } catch (InstantiationException ex) {
                throw new PredicateException(ex);
            } catch (IllegalAccessException ex) {
                throw new PredicateException(ex);
            } catch (java.lang.reflect.InvocationTargetException ex) {
                throw new PredicateException(ex);
            }
            if (motor instanceof StoreAware) {
                ((StoreAware) motor).setStore(this);
            }
            motors.add(motor);
        }
        Logger.debug(
            this,
            "#motors(): %d motors discovered in classpath: %[list]s",
            motors.size(),
            motors
        );
        return motors;
    }

}
