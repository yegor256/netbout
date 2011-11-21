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

import com.netbout.spi.Entry;
import com.netbout.spi.Helper;
import com.netbout.spi.HelperException;
import com.ymock.util.Logger;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang.StringUtils;
import org.reflections.Reflections;

/**
 * Classpath annotations helper.
 *
 * <p>Your classes should be annotated with <tt>&#64;Farm</tt> and
 * <tt>&#64;Operation</tt> annotations. Every operation should accept one of
 * following types: {@link Long}, {@link String}, {@link Boolean}.
 * Every operation should return one of the
 * following types: <tt>void</tt>, {@link String}, {@link Long},
 * {@link Boolean}, and an array of {@link Long}. All other types will lead
 * to runtime exception in {@link #CpaHelper(String)} constructor.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class CpaHelper implements Helper {

    /**
     * Name of package we're discovering.
     */
    private final transient String pkg;

    /**
     * The entry we're working with.
     */
    private transient Entry entry;

    /**
     * All discovered operations.
     */
    private transient ConcurrentMap<String, HelpTarget> ops;

    /**
     * Public ctor.
     * @param name Name of the package where to look for annotated methods
     *  and farms
     */
    public CpaHelper(final String name) {
        this.pkg = name;
    }

    /**
     * Public ctor.
     * @param type Use it to get name of package
     */
    public CpaHelper(final Class type) {
        this(type.getPackage().getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final Entry ent) throws HelperException {
        final long start = System.currentTimeMillis();
        this.entry = ent;
        this.ops = this.discover();
        Logger.debug(
            this,
            "#init('%s'): %d targets discovered in %dms",
            ent.getClass().getName(),
            this.ops.size(),
            System.currentTimeMillis() - start
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> supports() throws HelperException {
        if (this.ops == null) {
            throw new HelperException("Helper wasn't initialized with init()");
        }
        return this.ops.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(final String mnemo, final String... args)
        throws HelperException {
        if (!this.ops.containsKey(mnemo)) {
            throw new HelperException("Operation not supported");
        }
        final long start = System.currentTimeMillis();
        final String result = this.ops.get(mnemo).execute(args);
        Logger.debug(
            this,
            "#execute('%s', '%s'): done with '%s' in %dms",
            mnemo,
            StringUtils.join(args, "', '"),
            result,
            System.currentTimeMillis() - start
        );
        return result;
    }

    /**
     * Discover all targets and return them.
     * @return Associative array of discovered targets/operations
     */
    private ConcurrentMap<String, HelpTarget> discover() {
        final ConcurrentMap<String, HelpTarget> targets =
            new ConcurrentHashMap<String, HelpTarget>();
        final Reflections reflections = new Reflections(this.pkg);
        for (Class tfarm : reflections.getTypesAnnotatedWith(Farm.class)) {
            Logger.info(
                this,
                "#discover(%s): @Farm found at '%s'",
                this.pkg,
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
            if (farm instanceof EntryAwareFarm) {
                ((EntryAwareFarm) farm).init(this.entry);
            }
            targets.putAll(this.inFarm(farm));
        }
        return targets;
    }

    /**
     * Discover all methods in the provided farm.
     * @param farm The object annotated with {@link Farm}
     * @return Associative array of discovered targets/operations
     */
    private ConcurrentMap<String, HelpTarget> inFarm(final Object farm) {
        final ConcurrentMap<String, HelpTarget> targets =
            new ConcurrentHashMap<String, HelpTarget>();
        for (Method method : farm.getClass().getDeclaredMethods()) {
            final Annotation atn = method.getAnnotation(Operation.class);
            if (atn == null) {
                continue;
            }
            final String mnemo = ((Operation) atn).value();
            targets.put(mnemo, HelpTarget.build(farm, method));
            Logger.info(
                this,
                "#inFarm(%s): @Operation('%s') found",
                farm.getClass().getName(),
                mnemo
            );
        }
        return targets;
    }

}
