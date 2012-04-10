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
import com.netbout.spi.Urn;
import com.netbout.spi.UrnMocker;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;
import org.apache.commons.collections.IteratorUtils;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
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
     * The type.
     */
    private final transient Class<? extends Triples> type;

    /**
     * The implementation.
     */
    private transient Triples triples;

    /**
     * The random.
     */
    private final transient Random random = new Random();

    /**
     * Public ctor.
     * @param tpe The type
     */
    public TriplesTest(final Class<? extends Triples> tpe) {
        this.type = tpe;
    }

    /**
     * Get all implementations.
     * @return Collection of implementations
     */
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        final Collection<Object[]> args = new LinkedList<Object[]>();
        args.add(new Object[] {BerkeleyTriples.class});
        return args;
    }

    /**
     * Start triples.
     * @throws Exception If there is some problem inside
     */
    @Before
    public void start() throws Exception {
        this.triples = this.type
            .getConstructor(File.class)
            .newInstance(Files.createTempDir());
    }

    /**
     * Close the implementation.
     * @throws Exception If there is some problem inside
     */
    @After
    public void close() throws Exception {
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
        MatcherAssert.assertThat(
            this.triples.has(number, name, "some other value"),
            Matchers.is(false)
        );
    }

    /**
     * Triples can find single value.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsSingleValue() throws Exception {
        final String name = "the-name";
        final Long number = this.random.nextLong();
        final Urn urn = new UrnMocker().mock();
        this.triples.put(number, name, urn);
        this.triples.put(this.random.nextLong(), name, "other value");
        MatcherAssert.assertThat(
            this.triples.<Urn>get(number, name),
            Matchers.equalTo(urn)
        );
    }

    /**
     * Triples can throw if key-value pair is not found.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = MissedTripleException.class)
    public void throwsIfKeyValuePairNotFound() throws Exception {
        this.triples.get(this.random.nextLong(), "foo-foo-foo");
    }

    /**
     * Triples can clear some records.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void clearsRecords() throws Exception {
        final String name = "some-name-of-triple";
        final Long number = this.random.nextLong();
        final Long value = this.random.nextLong();
        this.triples.put(number, name, value);
        this.triples.clear(number, name);
        MatcherAssert.assertThat(
            this.triples.has(number, name, value),
            Matchers.is(false)
        );
    }

    /**
     * Triples can find all values.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsAllValues() throws Exception {
        final String name = "bar-bar-bar";
        final Long number = this.random.nextLong();
        final Urn first = new UrnMocker().mock();
        final Urn second = new UrnMocker().mock();
        this.triples.put(number, name, first);
        this.triples.put(number, name, second);
        MatcherAssert.assertThat(
            IteratorUtils.toList(this.triples.<Urn>all(number, name)),
            Matchers.allOf(
                (Matcher) Matchers.hasSize(2),
                Matchers.hasItem(first),
                Matchers.hasItem(second)
            )
        );
    }

}
