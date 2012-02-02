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

import com.netbout.bus.Bus;
import com.netbout.inf.predicates.CustomPred;
import com.netbout.spi.Urn;
import com.ymock.util.Logger;
import java.util.List;
import java.util.Map;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenStream;
import org.apache.commons.lang.ArrayUtils;

/**
 * Builder of a predicate.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class PredicateBuilder {

    /**
     * All functions and their classes.
     */
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    private static final Map<String, String> FUNCS = ArrayUtils.toMap(
        new String[][] {
            {"and", "com.netbout.inf.predicates.logic.AndPred"},
            {"equal", "com.netbout.inf.predicates.math.EqualPred"},
            {"from", "com.netbout.inf.predicates.FromPred"},
            {"greater-than", "com.netbout.inf.predicates.math.GreaterThanPred"},
            {"less-than", "com.netbout.inf.predicates.math.LessThanPred"},
            {"limit", "com.netbout.inf.predicates.LimitPred"},
            {"matches", "com.netbout.inf.predicates.text.MatchesPred"},
            {"not", "com.netbout.inf.predicates.logic.NotPred"},
            {"ns", "com.netbout.inf.predicates.xml.NsPred"},
            {"or", "com.netbout.inf.predicates.logic.OrPred"},
            {"pos", "com.netbout.inf.predicates.PosPred"},
            {"seen-by", "com.netbout.inf.predicates.SeenByPred"},
            {"talks-with", "com.netbout.inf.predicates.TalksWithPred"},
        }
    );

    /**
     * BUS to find custom predicates.
     */
    private final transient Bus ibus;

    /**
     * Public ctor.
     * @param bus The bus to work with
     */
    public PredicateBuilder(final Bus bus) {
        this.ibus = bus;
    }

    /**
     * Build a predicate from a query string.
     * @param query The query
     * @return The predicate
     */
    public Predicate parse(final String query) {
        final CharStream input = new ANTLRStringStream(
            this.normalize(query)
        );
        final QueryLexer lexer = new QueryLexer(input);
        final TokenStream tokens = new CommonTokenStream(lexer);
        final QueryParser parser = new QueryParser(tokens);
        parser.setPredicateBuilder(this);
        Predicate predicate;
        try {
            predicate = parser.query();
        } catch (org.antlr.runtime.RecognitionException ex) {
            throw new PredicateException(query, ex);
        } catch (PredicateException ex) {
            throw new PredicateException(query, ex);
        }
        Logger.debug(
            this,
            "#parse('%s'): predicate found: '%s'",
            query,
            predicate
        );
        return predicate;
    }

    /**
     * Normalize the query.
     * @param query Raw format
     * @return The text for predicate
     */
    public static String normalize(final String query) {
        String normalized;
        if (query == null) {
            normalized = PredicateBuilder.normalize("");
        } else if (query.startsWith("(") && query.endsWith(")")) {
            normalized = query;
        } else {
            normalized = String.format(
                // @checkstyle LineLength (1 line)
                "(or (matches '%s' $text) (matches '%1$s' $bout.title) (matches '%1$s' $author.alias))",
                query.replace("'", "\\'")
            );
        }
        return normalized;
    }

    /**
     * Build a predicate from name and list of preds.
     * @param name Its name
     * @param preds List of arguments
     * @return The predicate
     */
    protected Predicate build(final String name, final List<Predicate> preds) {
        Predicate predicate;
        if (this.FUNCS.containsKey(name)) {
            try {
                predicate = (Predicate) Class.forName(this.FUNCS.get(name))
                    .getConstructor(List.class)
                    .newInstance(preds);
            } catch (ClassNotFoundException ex) {
                throw new PredicateException(ex);
            } catch (NoSuchMethodException ex) {
                throw new PredicateException(ex);
            } catch (InstantiationException ex) {
                throw new PredicateException(ex);
            } catch (IllegalAccessException ex) {
                throw new PredicateException(ex);
            } catch (java.lang.reflect.InvocationTargetException ex) {
                throw new PredicateException(ex);
            }
        } else if (Urn.isValid(name)) {
            predicate = new CustomPred(this.ibus, Urn.create(name), preds);
        } else {
            throw new PredicateException(
                String.format("Unknown function '%s'", name)
            );
        }
        return predicate;
    }

}
