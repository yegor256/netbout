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
package com.netbout.lite.terms;

import com.netbout.lite.Term;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.WordUtils;

/**
 * Abstract term.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public abstract class AbstractTerm implements Term {

    /**
     * The list of arguments.
     */
    private final transient List<Term> arguments;

    /**
     * Public ctor.
     * @param args Arguments
     */
    public AbstractTerm(final List<Term> args) {
        this.arguments = new ArrayList<Term>(args);
    }

    /**
     * Create new text term.
     * @param text The text to encapsulate
     * @return Term created
     */
    public static Term text(final String text) {
        return new TextTerm(text);
    }

    /**
     * Create new var term.
     * @param name Variable name
     * @return Term created
     */
    public static Term var(final String name) {
        return new VarTerm(name);
    }

    /**
     * Create new term.
     * @param name Name of it
     * @param args Arguments
     * @return Term created
     */
    public static Term create(final String name, final List<Term> args) {
        try {
            final Class<?> type = Class.forName(
                String.format(
                    "%s.%sTerm",
                    AbstractTerm.class.getPackage().getName(),
                    WordUtils.capitalize(name)
                )
            );
            return Term.class.cast(
                type.getConstructor(List.class).newInstance(args)
            );
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException(ex);
        } catch (InstantiationException ex) {
            throw new IllegalArgumentException(ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException(ex);
        } catch (java.lang.reflect.InvocationTargetException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        final StringBuilder text = new StringBuilder();
        text.append('(');
        text.append(this.getClass().getSimpleName());
        for (Term arg : this.args()) {
            text.append(' ');
            text.append(arg.toString());
        }
        text.append(')');
        return text.toString();
    }

    /**
     * Arguments.
     * @return List of them
     */
    protected final List<Term> args() {
        return this.arguments;
    }

}
