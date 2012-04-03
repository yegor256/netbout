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

import com.netbout.spi.Bout;
import com.netbout.spi.BoutMocker;
import com.netbout.spi.Message;
import com.netbout.spi.MessageMocker;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link SeeMessageTask}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class SeeMessageTaskTest {

    /**
     * SeeMessageTask can handle many message updates in parallel.
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings({
        "PMD.AvoidInstantiatingObjectsInLoops", "PMD.AvoidCatchingThrowable"
    })
    public void handlesMessageUpdatesInParallel() throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(10);
        final Collection<Callable> tasks = new ArrayList<Callable>();
        final AtomicLong failed = new AtomicLong(50);
        final Index index = new IndexMocker().mock();
        for (int idx = 0; idx < failed.get(); idx += 1) {
            tasks.add(
                new Callable<Long>() {
                    @Override
                    public Long call() {
                        try {
                            final Bout bout = new BoutMocker()
                                .withDate(new Date())
                                .mock();
                            final Message msg = new MessageMocker()
                                .withDate(new Date())
                                .inBout(bout)
                                .mock();
                            new SeeMessageTask(msg, index).run();
                        // @checkstyle IllegalCatch (1 line)
                        } catch (Throwable ex) {
                            Logger.error(this, "%[exception]s", ex);
                            throw new IllegalStateException(ex);
                        }
                        return failed.getAndDecrement();
                    }
                }
            );
        }
        executor.invokeAll((Collection) tasks);
        MatcherAssert.assertThat(failed.get(), Matchers.equalTo(0L));
    }

}
