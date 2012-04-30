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

import com.netbout.inf.notices.MessagePostedNotice;
import com.netbout.spi.Message;
import com.netbout.spi.MessageMocker;
import com.netbout.spi.Urn;
import com.netbout.spi.UrnMocker;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link Mux}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
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
        final Mux mux = new Mux(
            new RayMocker().mock(),
            new StoreMocker().mock()
        );
        final Urn name = new UrnMocker().mock();
        final CountDownLatch latch = new CountDownLatch(100);
        for (int idx = 0; idx < latch.getCount(); idx += 1) {
            mux.add(
                new MessagePostedNotice() {
                    @Override
                    public Message message() {
                        return new MessageMocker().mock();
                    }
                }
            );
        }
        latch.await(1, TimeUnit.SECONDS);
        MatcherAssert.assertThat(mux.eta(name), Matchers.equalTo(0L));
        mux.close();
    }
    /**
     * Mux can render statistics.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void rendersStatistics() throws Exception {
        MatcherAssert.assertThat(
            new Mux(new RayMocker().mock(), new StoreMocker().mock()),
            Matchers.hasToString(Matchers.notNullValue())
        );
    }

}
