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
package com.netbout.inf.ray;

import com.netbout.inf.Cursor;
import com.netbout.inf.CursorMocker;
import java.util.HashSet;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link DefaultCache}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class DefaultCacheTest {

    /**
     * DefaultCache can cache shifting calls.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void cachesShiftCalls() throws Exception {
        final Cache cache = new DefaultCache();
        final CountingTerm term = new CountingTerm("hello");
        final Cursor cursor = new CursorMocker().mock();
        cache.shift(term, cursor);
        cache.shift(term, cursor);
        MatcherAssert.assertThat(
            term.count(),
            Matchers.equalTo(1)
        );
    }

    /**
     * DefaultCache can clear itself after shifting.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void clearsItselfAfterCaching() throws Exception {
        final Cache cache = new DefaultCache();
        final String attr = "the-attribute";
        final CountingTerm term = new CountingTerm(attr);
        final Cursor cursor = new CursorMocker().mock();
        cache.shift(term, cursor);
        cache.clear(attr);
        cache.shift(term, cursor);
        MatcherAssert.assertThat(
            term.count(),
            Matchers.equalTo(2)
        );
    }

    private final class CountingTerm implements DependableTerm, Cacheable {
        /**
         * Attribute name it depends on.
         */
        private final transient String attr;
        /**
         * How many times shift() was called.
         */
        private transient int calls;
        /**
         * Public ctor.
         * @param txt The attribute
         */
        public CountingTerm(final String txt) {
            this.attr = txt;
        }
        /**
         * How many times shift() was called.
         * @return Count of times
         */
        public int count() {
            return this.calls;
        }
        @Override
        public boolean cacheThis() {
            return true;
        }
        @Override
        public Set<DependableTerm.Dependency> dependencies() {
            final Set<DependableTerm.Dependency> deps =
                new HashSet<DependableTerm.Dependency>();
            deps.add(new DependableTerm.Dependency(this.attr));
            return deps;
        }
        @Override
        public Cursor shift(final Cursor cursor) {
            ++this.calls;
            return cursor;
        }
    }

}
