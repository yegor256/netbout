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
import com.ymock.util.Logger;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.io.FileUtils;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
     * Temp dir.
     */
    private static File dir;

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
        // args.add(new Object[] {BerkeleyTriples.class});
        args.add(new Object[] {HsqlTriples.class});
        return args;
    }

    /**
     * Create a temp dir for the entire test.
     * @throws Exception If there is some problem inside
     */
    @BeforeClass
    public static void tempDir() throws Exception {
        TriplesTest.dir = Files.createTempDir();
    }

    /**
     * Remove a temp dir.
     * @throws Exception If there is some problem inside
     */
    @AfterClass
    public static void delDir() throws Exception {
        FileUtils.deleteDirectory(TriplesTest.dir);
    }

    /**
     * Start triples.
     * @throws Exception If there is some problem inside
     */
    @Before
    public void start() throws Exception {
        this.triples = this.type
            .getConstructor(File.class)
            .newInstance(new File(TriplesTest.dir, this.type.getName()));
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

    /**
     * Triples can reverse value to an iterator of numbers.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void reversesSingleValueToIterator() throws Exception {
        final String name = "a-simple-name-of-triple";
        final Long first = this.random.nextLong();
        final Long second = first + 1L;
        final Urn value = new UrnMocker().mock();
        this.triples.put(first, name, value);
        this.triples.put(second, name, value);
        MatcherAssert.assertThat(
            IteratorUtils.toList(this.triples.reverse(name, value)),
            Matchers.allOf(
                (Matcher) Matchers.hasSize(2),
                Matchers.hasItems(second, first)
            )
        );
    }

    /**
     * Triples can join two triples.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void joinTwoTriples() throws Exception {
        final String left = "left-triple";
        final String right = "right-triple";
        final Long number = this.random.nextLong();
        final Long joiner = this.random.nextLong();
        this.triples.put(number, left, joiner);
        final Urn value = new UrnMocker().mock();
        this.triples.put(joiner, right, value);
        MatcherAssert.assertThat(
            IteratorUtils.toList(this.triples.reverse(left, right, value)),
            Matchers.allOf(
                (Matcher) Matchers.hasSize(1),
                Matchers.hasItems(number)
            )
        );
    }

    /**
     * Triples can re-use the files.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void reusesFilesOnDiscAfterClose() throws Exception {
        final String name = "boom-boom";
        final Long number = this.random.nextLong();
        final Urn value = new UrnMocker().mock();
        this.triples.put(number, name, value);
        this.close();
        this.start();
        this.triples.put(number + 1, name, value);
        MatcherAssert.assertThat(
            this.triples.has(number, name, value),
            Matchers.is(true)
        );
    }

    /**
     * Triples can work in multiple threads.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void supportsMultiThreadedOperations() throws Exception {
        final String name = "boom-boom-multi-thread";
        final Long number = this.random.nextLong();
        final Urn value = new UrnMocker().mock();
        TriplesTest.this.triples.put(number, name, value);
        // @checkstyle MagicNumber (1 line)
        final int threads = 200;
        final AtomicInteger succeeded = new AtomicInteger(0);
        final Callable<Boolean> task = new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    TriplesTest.this.triples.put(
                        TriplesTest.this.random.nextLong(),
                        name,
                        value
                    );
                    MatcherAssert.assertThat(
                        TriplesTest.this.triples.has(number, name, value),
                        Matchers.is(true)
                    );
                    MatcherAssert.assertThat(
                        IteratorUtils.toList(
                            TriplesTest.this.triples.reverse(name, value)
                        ),
                        Matchers.hasItems(number)
                    );
                } catch (Throwable ex) {
                    Logger.error(this, "%[exception]s", ex);
                    throw new IllegalStateException(ex);
                }
                succeeded.incrementAndGet();
                return true;
            }
        };
        final Collection<Callable<Boolean>> tasks =
            new ArrayList<Callable<Boolean>>(threads / 2);
        for (int thread = 0; thread < threads; ++thread) {
            tasks.add(task);
        }
        Executors.newFixedThreadPool(threads).invokeAll(tasks);
        MatcherAssert.assertThat(
            succeeded.get(),
            Matchers.equalTo(threads)
        );
    }

}
