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
package com.netbout.lite;

import com.jcabi.log.Logger;
import com.netbout.spi.Message;
import com.netbout.spi.Query;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenStream;

/**
 * Simple term, based on {@link Query}.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id: ParserAdapter.java 3465 2012-10-16 18:31:35Z guard $
 */
final class QueryTerm implements Term {

    /**
     * The composite term.
     */
    private final transient Term term;

    /**
     * Public ctor.
     * @param query Original query
     */
    public QueryTerm(final Query query) {
        final CharStream input = new ANTLRStringStream(query.toString());
        final QueryLexer lexer = new QueryLexer(input);
        final TokenStream tokens = new CommonTokenStream(lexer);
        final QueryParser parser = new QueryParser(tokens);
        try {
            this.term = parser.term();
        } catch (org.antlr.runtime.RecognitionException ex) {
            throw new IllegalArgumentException(query.toString(), ex);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(query.toString(), ex);
        }
        Logger.debug(
            this,
            "#QueryTerm('%[text]s'): parsed to '%s'",
            query,
            this.term
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(final Message message) {
        return this.term.matches(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value(final Message message) {
        throw new UnsupportedOperationException(
            "QueryTerm doesn't have a value"
        );
    }

}
