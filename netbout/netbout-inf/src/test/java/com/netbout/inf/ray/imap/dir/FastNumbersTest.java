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
 * this code accidentally and without intent to use it, please report this
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
package com.netbout.inf.ray.imap.dir;

import com.jcabi.log.VerboseThreads;
import com.netbout.inf.MsgMocker;
import com.netbout.inf.ray.imap.Numbers;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link FastNumbers}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle MagicNumber (500 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class FastNumbersTest {

    /**
     * FastNumbers can save to stream and restore.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void savesAndRestores() throws Exception {
        final Numbers numbers = new FastNumbers();
        final long msg = MsgMocker.number();
        for (int num = 0; num < 1000; ++num) {
            numbers.add(msg + num);
        }
        MatcherAssert.assertThat(numbers.next(msg + 1), Matchers.equalTo(msg));
        final ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        numbers.save(ostream);
        final byte[] data = ostream.toByteArray();
        final Numbers restored = new FastNumbers();
        final InputStream istream = new ByteArrayInputStream(data);
        restored.load(istream);
        MatcherAssert.assertThat(restored.next(msg + 1), Matchers.equalTo(msg));
        MatcherAssert.assertThat(restored.next(msg), Matchers.equalTo(0L));
    }

    /**
     * FastNumbers can accept specific numbers, and deal with them correctly.
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void acceptsSpecificNumbers() throws Exception {
        final long[][] samples = new long[][] {
            {9, 7, 5, 3, 1},
            {9, 8, 7, 6, 5, 4},
            {4, 5, 6, 7, 8, 9},
            {19, 18, 17, 16, 5, 4, 3, 2},
            {2, 3, 4, 5, 16, 17, 18, 19},
        };
        for (long[] sample : samples) {
            final Numbers numbers = new FastNumbers();
            for (int pos = 0; pos < sample.length; ++pos) {
                numbers.add(sample[pos]);
                numbers.add(sample[pos]);
                MatcherAssert.assertThat(
                    numbers.next(sample[pos] + 1),
                    Matchers.equalTo(sample[pos])
                );
            }
            for (int pos = 0; pos < sample.length; ++pos) {
                numbers.remove(sample[pos]);
                numbers.remove(sample[pos]);
                MatcherAssert.assertThat(
                    numbers.next(sample[pos] + 1),
                    Matchers.not(Matchers.equalTo(sample[pos]))
                );
            }
        }
    }

    /**
     * FastNumbers can big amount of numbers.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void acceptsManyNumbersInRow() throws Exception {
        final Numbers numbers = new FastNumbers();
        final long msg = MsgMocker.number();
        numbers.add(msg);
        final long total = 50000;
        for (int num = 1; num < total; ++num) {
            numbers.add(msg + num);
            MatcherAssert.assertThat(
                numbers.next(msg + num + 1),
                Matchers.equalTo(msg + num)
            );
        }
    }

    /**
     * FastNumbers can handle boundary conditions correctly.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void handlesBoundaryConditions() throws Exception {
        final Numbers numbers = new FastNumbers();
        MatcherAssert.assertThat(numbers.next(1L), Matchers.equalTo(0L));
        MatcherAssert.assertThat(numbers.next(0L), Matchers.equalTo(0L));
        MatcherAssert.assertThat(
            numbers.next(Long.MAX_VALUE),
            Matchers.equalTo(0L)
        );
    }

    /**
     * FastNumbers can throw on MAX_VALUE.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = IllegalArgumentException.class)
    public void doestAcceptMaxValue() throws Exception {
        new FastNumbers().add(Long.MAX_VALUE);
    }

    /**
     * FastNumbers can throw on ZERO.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = IllegalArgumentException.class)
    public void doestAcceptZero() throws Exception {
        new FastNumbers().add(0L);
    }

    /**
     * FastNumbers can throw on MAX_VALUE.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = IllegalArgumentException.class)
    public void doestRemoveMaxValue() throws Exception {
        new FastNumbers().remove(Long.MAX_VALUE);
    }

    /**
     * FastNumbers can throw on ZERO.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = IllegalArgumentException.class)
    public void doestRemoveZero() throws Exception {
        new FastNumbers().remove(0L);
    }

    /**
     * FastNumbers can accept many numbers.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void acceptsManyNumbers() throws Exception {
        final Numbers numbers = new FastNumbers();
        final long msg = MsgMocker.number();
        numbers.add(msg);
        final List<Long> all = new ArrayList<Long>();
        for (int num = 1; num < 100; ++num) {
            all.add(msg + num);
        }
        Collections.shuffle(all);
        for (Long num : all) {
            numbers.add(num);
            MatcherAssert.assertThat(
                numbers.next(num + 1),
                Matchers.equalTo(num)
            );
        }
        for (Long num : all) {
            MatcherAssert.assertThat(
                numbers.next(num),
                Matchers.equalTo(num - 1)
            );
        }
        for (Long num : all) {
            numbers.remove(num);
            MatcherAssert.assertThat(
                numbers.next(num + 1),
                Matchers.not(Matchers.equalTo(num))
            );
        }
        for (Long num : all) {
            MatcherAssert.assertThat(
                numbers.next(num),
                Matchers.equalTo(msg)
            );
        }
        numbers.remove(msg);
        MatcherAssert.assertThat(numbers.isEmpty(), Matchers.is(true));
    }

    /**
     * FastNumbers can add numbers and find them later.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void addsNumbersAndFindsThemLater() throws Exception {
        final Numbers numbers = new FastNumbers();
        final long msg = MsgMocker.number();
        for (int num = 0; num < 5; ++num) {
            numbers.add(msg + num);
            MatcherAssert.assertThat(
                numbers.next(msg + num + 1),
                Matchers.equalTo(msg + num)
            );
        }
        MatcherAssert.assertThat(numbers.next(msg), Matchers.equalTo(0L));
    }

    /**
     * FastNumbers can add duplicate numbers.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void addsDuplicateNumbersAndFindsThemLater() throws Exception {
        final Numbers numbers = new FastNumbers();
        final long msg = MsgMocker.number();
        for (int num = 0; num < 5; ++num) {
            numbers.add(msg + num);
        }
        numbers.add(msg + 1);
        MatcherAssert.assertThat(numbers.next(msg + 1), Matchers.equalTo(msg));
    }

    /**
     * FastNumbers can delete a number.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void deletesNumberAndDoesntFindItLater() throws Exception {
        final Numbers numbers = new FastNumbers();
        final long msg = MsgMocker.number();
        numbers.add(msg);
        numbers.remove(msg);
        MatcherAssert.assertThat(numbers.next(msg + 1), Matchers.equalTo(0L));
    }

    /**
     * FastNumbers can calculate its size in bytes.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void calculatesSizeInBytes() throws Exception {
        final Numbers numbers = new FastNumbers();
        final long msg = MsgMocker.number();
        numbers.add(msg);
        MatcherAssert.assertThat(numbers.sizeof(), Matchers.greaterThan(0L));
    }

    /**
     * FastNumbers can produce a lattice.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void producesLattice() throws Exception {
        final Numbers numbers = new FastNumbers();
        final long msg = MsgMocker.number();
        numbers.add(msg);
        MatcherAssert.assertThat(numbers.lattice(), Matchers.notNullValue());
    }

    /**
     * FastNumbers can add and find in parallel threads.
     * @throws Exception If there is some problem inside
     * @checkstyle ExecutableStatementCount (100 lines)
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void searchesInParallelThreads() throws Exception {
        final Numbers numbers = new FastNumbers();
        final int threads = Runtime.getRuntime().availableProcessors();
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch latch = new CountDownLatch(threads);
        final Callable<?> task = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                start.await();
                final long msg = MsgMocker.number();
                numbers.add(msg);
                MatcherAssert.assertThat(
                    numbers.next(msg + 1),
                    Matchers.equalTo(msg)
                );
                latch.countDown();
                return null;
            }
        };
        final ExecutorService svc =
            Executors.newFixedThreadPool(threads, new VerboseThreads());
        for (int thread = 0; thread < threads; ++thread) {
            svc.submit(task);
        }
        start.countDown();
        MatcherAssert.assertThat(
            latch.await(1, TimeUnit.MINUTES),
            Matchers.is(true)
        );
        svc.shutdown();
    }

}
