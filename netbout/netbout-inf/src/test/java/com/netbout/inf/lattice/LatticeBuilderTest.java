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
package com.netbout.inf.lattice;

import com.jcabi.log.VerboseThreads;
import com.netbout.inf.Cursor;
import com.netbout.inf.CursorMocker;
import com.netbout.inf.Lattice;
import com.netbout.inf.MsgMocker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case of {@link LatticeBuilder}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle MagicNumber (500 lines)
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings({
    "PMD.AvoidInstantiatingObjectsInLoops", "PMD.TooManyMethods"
})
public final class LatticeBuilderTest {

    /**
     * LatticeBuilder can create lattice from numbers.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void createsLatticeFromNumbers() throws Exception {
        for (int nums = 0; nums < 25; ++nums) {
            final SortedSet<Long> numbers = LatticeBuilderTest.numbers(nums);
            final LatticeBuilder builder = new LatticeBuilder().never();
            for (Long number : LatticeBuilderTest.shuffle(numbers)) {
                builder.update(number, numbers);
            }
            MatcherAssert.assertThat(
                new LatticeBuilder().fill(numbers).build(),
                Matchers.equalTo(builder.build())
            );
            MatcherAssert.assertThat(
                new LatticeBuilder().fill(numbers).revert().build(),
                Matchers.equalTo(builder.revert().build())
            );
        }
    }

    /**
     * LatticeBuilder can create reverse lattice correctly.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void createsAndFillsReverseLattice() throws Exception {
        final Long[][] samples = new Long[][] {
            {500L, 135L, 134L},
            {5L, 4L, 3L, 2L, 1L},
            {505L, 504L, 503L, 502L},
            {129L, 128L, 64L, 63L},
        };
        for (Long[] sample : samples) {
            final SortedSet<Long> numbers = new TreeSet<Long>(
                Collections.reverseOrder()
            );
            numbers.addAll(Arrays.asList(sample));
            final LatticeBuilder builder = new LatticeBuilder().never();
            for (Long number : numbers) {
                builder.update(number, numbers);
            }
            MatcherAssert.assertThat(
                new LatticeBuilder().fill(numbers).revert().build(),
                Matchers.equalTo(builder.revert().build())
            );
        }
    }

    /**
     * LatticeBuilder can throw exception for ZERO message number.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = LatticeException.class)
    public void throwsWhenZeroMesageNumber() throws Exception {
        new LatticeBuilder()
            .never()
            .update(0, new TreeSet<Long>(Collections.reverseOrder()));
    }

    /**
     * LatticeBuilder can throw exception for MAX message number.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = LatticeException.class)
    public void throwsWhenMaxMesageNumber() throws Exception {
        new LatticeBuilder()
            .always()
            .update(
                Long.MAX_VALUE,
                new TreeSet<Long>(Collections.reverseOrder())
            );
    }

    /**
     * LatticeBuilder can convert numbers to bits.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void convertsNumbersToBits() throws Exception {
        MatcherAssert.assertThat(
            BitsetLattice.bit(1L),
            Matchers.equalTo(BitsetLattice.BITS - 1)
        );
        MatcherAssert.assertThat(
            BitsetLattice.bit(BitsetLattice.SIZE),
            Matchers.equalTo(BitsetLattice.BITS - 1)
        );
        MatcherAssert.assertThat(
            BitsetLattice.bit(BitsetLattice.SIZE + 1),
            Matchers.equalTo(BitsetLattice.BITS - 2)
        );
        MatcherAssert.assertThat(
            BitsetLattice.bit((BitsetLattice.BITS - 1) * BitsetLattice.SIZE),
            Matchers.equalTo(1)
        );
        MatcherAssert.assertThat(
            BitsetLattice.bit(BitsetLattice.BITS * BitsetLattice.SIZE),
            Matchers.equalTo(0)
        );
    }

    /**
     * LatticeBuilder can convert bits to numbers.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void convertsBitsToNumbers() throws Exception {
        MatcherAssert.assertThat(
            BitsetLattice.msg(0),
            Matchers.equalTo((long) BitsetLattice.BITS * BitsetLattice.SIZE)
        );
        MatcherAssert.assertThat(
            BitsetLattice.msg(1),
            Matchers.equalTo(
                (long) (BitsetLattice.BITS - 1) * BitsetLattice.SIZE
            )
        );
        MatcherAssert.assertThat(
            BitsetLattice.msg(BitsetLattice.BITS - 2),
            Matchers.equalTo((long) BitsetLattice.SIZE * 2)
        );
        MatcherAssert.assertThat(
            BitsetLattice.msg(BitsetLattice.BITS - 1),
            Matchers.equalTo((long) BitsetLattice.SIZE)
        );
    }

    /**
     * LatticeBuilder can accept any number for an empty set.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void acceptsAnyNumberOnEmptySet() throws Exception {
        final LatticeBuilder builder = new LatticeBuilder().never();
        final SortedSet<Long> numbers =
            new TreeSet<Long>(Collections.reverseOrder());
        builder.update(1L, numbers);
        builder.update(BitsetLattice.BITS * BitsetLattice.SIZE, numbers);
        final Random random = new Random();
        for (int retry = 0; retry < 100; ++retry) {
            builder.update(
                random.nextInt(BitsetLattice.BITS * BitsetLattice.SIZE - 1) + 1,
                numbers
            );
        }
    }

    /**
     * LatticeBuilder can build lattice in multiple threads.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void createsLatticeInMultipleThreads() throws Exception {
        final SortedSet<Long> numbers = LatticeBuilderTest.numbers(10);
        final LatticeBuilder builder = new LatticeBuilder();
        final int threads = 10;
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch latch = new CountDownLatch(threads);
        final Callable<?> task = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                start.await();
                builder.fill(numbers);
                for (Long number : LatticeBuilderTest.shuffle(numbers)) {
                    builder.update(number, numbers);
                }
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
        latch.await(1, TimeUnit.SECONDS);
        svc.shutdown();
        MatcherAssert.assertThat(
            new LatticeBuilder().fill(numbers).build(),
            Matchers.equalTo(builder.build())
        );
    }

    /**
     * LatticeBuilder can create working lattice from numbers.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void createsWorkingLatticeFromNumbers() throws Exception {
        final Lattice.Shifter shifter = Mockito.mock(Lattice.Shifter.class);
        final Cursor cursor = new CursorMocker()
            .withMsg(Long.MAX_VALUE)
            .mock();
        new LatticeBuilder().fill(LatticeBuilderTest.numbers(10))
            .build().correct(cursor, shifter);
        Mockito.verify(shifter)
            .shift(Mockito.any(Cursor.class), Mockito.anyLong());
    }

    /**
     * LatticeBuilder can copy lattice.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void copiesLatticeWithAllNumbers() throws Exception {
        final Lattice lattice = new LatticeBuilder()
            .fill(LatticeBuilderTest.numbers(10))
            .build();
        MatcherAssert.assertThat(
            lattice,
            Matchers.equalTo(new LatticeBuilder().copy(lattice).build())
        );
    }

    /**
     * LatticeBuilder can revert lattice.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void revertsLatticeWithAllNumbers() throws Exception {
        final LatticeBuilder builder =
            new LatticeBuilder().fill(LatticeBuilderTest.numbers(5));
        final Lattice.Shifter shifter = Mockito.mock(Lattice.Shifter.class);
        final Cursor cursor = new CursorMocker()
            .withMsg(Long.MAX_VALUE)
            .mock();
        builder.build().correct(cursor, shifter);
        Mockito.verify(shifter)
            .shift(Mockito.any(Cursor.class), Mockito.anyLong());
        Mockito.reset(shifter);
        builder.revert().build().correct(cursor, shifter);
        Mockito.verify(shifter, Mockito.times(0))
            .shift(Mockito.any(Cursor.class), Mockito.anyLong());
    }

    /**
     * Create some numbers.
     * @param total How many numbers to create
     * @return Set of numbers
     */
    private static SortedSet<Long> numbers(final int total) {
        final SortedSet<Long> numbers =
            new TreeSet<Long>(Collections.reverseOrder());
        for (int num = 0; num < total; ++num) {
            numbers.add(MsgMocker.number());
        }
        return numbers;
    }

    /**
     * Create randomly ordered numbers.
     * @param numbers The numbers to order randomly
     * @return Collection of the same numbers, but ordered randomly
     */
    private static Collection<Long> shuffle(final Collection<Long> numbers) {
        final List<Long> list = new ArrayList<Long>(numbers);
        Collections.shuffle(list);
        return list;
    }

}
