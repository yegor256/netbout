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
import com.netbout.spi.Urn;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.List;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenStream;
import org.reflections.Reflections;

/**
 * Builder of a predicate.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class PredicateBuilder {

    /**
     * All predicates discovered in classpath.
     */
    private static final List<PredicateToken> PREDICATES =
        PredicateBuilder.discover();

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
     * Extract properties from the message.
     * @param from The message
     * @param msg Where to extract
     */
    @SuppressWarnings("PMD.DefaultPackage")
    static void extract(final Message from) {
        for (PredicateToken token : PredicateBuilder.PREDICATES) {
            token.extract(from);
        }
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
        } else if (!query.isEmpty() && query.charAt(0) == '('
            && query.endsWith(")")) {
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
     * @param atoms List of arguments
     * @return The predicate
     */
    protected Predicate build(final String name, final List<Atom> atoms) {
        Predicate predicate = null;
        for (PredicateToken token : this.PREDICATES) {
            if (token.namedAs(name)) {
                predicate = token.build(atoms);
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
     * @return List of predicate tokens
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private static List<PredicateToken> discover() {
        final Reflections ref = new Reflections(
            PredicateBuilder.class.getPackage().getName()
        );
        final List<PredicateToken> tokens = new ArrayList<PredicateToken>();
        for (Class pred : ref.getTypesAnnotatedWith(Meta.class)) {
            tokens.add(new PredicateToken(pred));
        }
        Logger.debug(
            PredicateBuilder.class,
            "#discover(): %d predicates discovered in classpath: %[list]s",
            tokens.size(),
            tokens
        );
        return tokens;
    }

}
