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
package com.netbout.inf.triples;

import com.google.common.io.Files;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Test case of {@link Triples} (and all of its implementations).
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@RunWith(Parameterized.class)
public final class TriplesTest {

    /**
     * The implementation.
     */
    private final transient Triples triples;

    /**
     * The random.
     */
    private final transient Random random = new Random();

    /**
     * Public ctor.
     * @param trpl The triples
     */
    public TriplesTest(final Triples trpl) {
        this.triples = trpl;
    }

    /**
     * Get all implementations.
     * @return Collection of implementations
     */
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        final Collection<Object[]> args = new LinkedList<Object[]>();
        args.add(new Object[] {new BerkleyTriples(Files.createTempDir())});
        return args;
    }

    /**
     * Close the implementation.
     * @throws Exception If there is some problem inside
     */
    @After
    public void closeTriple() throws Exception {
        this.triples.close();
    }

    /**
     * Triples can find just added triple.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsTripleJustAdded() throws Exception {
        final String name = "some-name";
        final Long number = this.random.nextLong();
        final String value = "some value to save";
        this.triples.put(number, name, value);
        MatcherAssert.assertThat(
            this.triples.has(number, name, value),
            Matchers.is(true)
        );
    }

}
