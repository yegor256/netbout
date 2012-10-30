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
package com.netbout.spi;

import java.util.regex.Pattern;

/**
 * Query.
 *
 * <p>Instances of this interface must be thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle InterfaceIsType (500 lines)
 */
public interface Query {

    /**
     * Pattern to match special queries.
     */
    Pattern FORMATTED = Pattern.compile("\\s*\\(.*\\)\\s*", Pattern.DOTALL);

    /**
     * Simple textual implementation of it.
     */
    class Textual implements Query {
        /**
         * Text of the query.
         */
        private final transient String text;
        /**
         * Public ctor.
         * @param txt The text of it
         */
        public Textual(final String txt) {
            this.text = Query.Textual.normalize(txt);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return this.text;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(final Object query) {
            return query == this || (query instanceof Query
                && this.toString().equals(query.toString()));
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return this.toString().hashCode();
        }
        /**
         * Normalize query.
         * @param query The query to normalize
         * @return Normalized query
         */
        private static String normalize(final String query) {
            String normalized;
            if (query == null) {
                normalized = Query.Textual.normalize("");
            } else if (Query.FORMATTED.matcher(query).matches()) {
                normalized = query.trim();
            } else {
                normalized = String.format(
                    "(matches '%s')",
                    query.replace("'", "\\'")
                );
            }
            return normalized;
        }
    }

}
