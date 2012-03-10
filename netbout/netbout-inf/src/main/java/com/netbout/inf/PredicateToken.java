/**
 * Copyright (c) 2009-2011, netBout.com
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

import com.netbout.spi.Message;
import java.util.List;

/**
 * Predicate wrapping token.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class PredicateToken {

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
    public PredicateToken(final Class<? extends Predicate> pred) {
        this.type = pred;
        this.meta = pred.getAnnotation(Meta.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.type.getName();
    }

    /**
     * Is it amed as.
     * @param name The name to check against
     * @return Matches?
     */
    public boolean namedAs(final String name) {
        return this.meta.name().equals(name);
    }

    /**
     * Extract properties from the message.
     * @param from The message
     * @param index The index
     */
    public void extract(final Message from, final Index index) {
        if (this.meta.extracts()) {
            try {
                this.type.getMethod("extract", Message.class, Index.class)
                    .invoke(null, from, index);
            } catch (NoSuchMethodException ex) {
                throw new PredicateException(ex);
            } catch (IllegalAccessException ex) {
                throw new PredicateException(ex);
            } catch (java.lang.reflect.InvocationTargetException ex) {
                throw new PredicateException(ex);
            }
        }
    }

    /**
     * Build a predicate from list of preds.
     * @param atoms List of arguments
     * @param index The index
     * @return The predicate
     */
    public Predicate build(final List<Atom> atoms, final Index index) {
        Predicate predicate;
        try {
            predicate = (Predicate) this.type
                .getConstructor(List.class, Index.class)
                .newInstance(atoms, index);
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

}
