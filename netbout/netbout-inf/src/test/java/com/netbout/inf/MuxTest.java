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

import com.netbout.spi.Urn;
import com.netbout.spi.UrnMocker;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link Mux}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings({ "PMD.DoNotUseThreads", "PMD.TestClassWithoutTestCases" })
public final class MuxTest {

    /**
     * The random to use.
     */
    private static final Random RANDOM = new SecureRandom();

    /**
     * Mux can run tasks in parallel.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void runsTasksInParallel() throws Exception {
        final Mux mux = new Mux();
        final Urn name = new UrnMocker().mock();
        final CountDownLatch latch = new CountDownLatch(100);
        final Task task = new FooTask(name, latch);
        for (int idx = 0; idx < latch.getCount(); idx += 1) {
            mux.add(task);
        }
        try {
            latch.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
        MatcherAssert.assertThat(mux.eta(name), Matchers.equalTo(0L));
        mux.close();
    }

    private static final class FooTask extends AbstractTask {
        /**
         * My name.
         */
        private final transient Urn name;
        /**
         * The latch to count down.
         */
        private final transient CountDownLatch latch;
        /**
         * Public ctor.
         * @param urn My name
         * @param ltch Latch to count down
         */
        public FooTask(final Urn urn, final CountDownLatch ltch) {
            super();
            this.name = urn;
            this.latch = ltch;
        }
        @Override
        protected void execute() {
            try {
                // @checkstyle MagicNumber (1 line)
                TimeUnit.MILLISECONDS.sleep((long) MuxTest.RANDOM.nextInt(10));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            this.latch.countDown();
        }
        @Override
        public String toString() {
            return this.name.toString();
        }
        @Override
        public Set<Urn> dependants() {
            final Set<Urn> names = new HashSet<Urn>();
            names.add(this.name);
            return names;
        }
    };

}
