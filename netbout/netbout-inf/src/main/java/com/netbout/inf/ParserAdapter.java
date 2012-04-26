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

import com.netbout.inf.atoms.PredicateAtom;
import com.ymock.util.Logger;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenStream;

/**
 * Adapter for ANTLR3 parser.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class ParserAdapter {

    /**
     * The store.
     */
    private final transient Store store;

    /**
     * Public ctor.
     * @param str The store with functors
     */
    public ParserAdapter(final Store str) {
        this.store = str;
    }

    /**
     * Build a predicate atom from a query string.
     * @param query The query
     * @return The predicate atom
     * @throws InvalidSyntaxException If query is not valid
     */
    public PredicateAtom parse(final String query)
        throws InvalidSyntaxException {
        final CharStream input = new ANTLRStringStream(
            NetboutUtils.normalize(query)
        );
        final QueryLexer lexer = new QueryLexer(input);
        final TokenStream tokens = new CommonTokenStream(lexer);
        final QueryParser parser = new QueryParser(tokens);
        parser.setStore(this.store);
        PredicateAtom predicate;
        try {
            predicate = parser.query();
        } catch (org.antlr.runtime.RecognitionException ex) {
            throw InvalidSyntaxException(query, ex);
        } catch (IllegalArgumentException ex) {
            throw InvalidSyntaxException(query, ex);
        }
        Logger.debug(
            this,
            "#parse('%s'): predicate '%s' parsed",
            query,
            predicate
        );
        return predicate;
    }

}
