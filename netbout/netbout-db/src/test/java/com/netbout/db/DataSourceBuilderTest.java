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
package com.netbout.db;

import com.netbout.spi.Urn;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link DataSourceBuilder}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class DataSourceBuilderTest {

    /**
     * The name to test against.
     */
    private static final Urn NAME = new IdentityRowMocker().mock();

    /**
     * DataSourceBuilder can handle many simultaneous connections.
     * @throws Exception If there is some problem inside
     * @checkstyle MagicNumber (10 lines)
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void canHandleSimultaneousConnections() throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(20);
        final Collection<Task> tasks = new ArrayList<Task>();
        for (int num = 0; num < 100; num += 1) {
            tasks.add(new Task(num));
        }
        for (Future<Boolean> future : executor.invokeAll(tasks)) {
            MatcherAssert.assertThat(future.get(), Matchers.equalTo(true));
        }
    }

    private static final class Task implements Callable<Boolean> {
        /**
         * Unique id.
         */
        private final transient int number;
        /**
         * Public ctor.
         * @param num Number of the task
         */
        public Task(final int num) {
            this.number = num;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("PMD.AvoidCatchingGenericException")
        public Boolean call() {
            Logger.debug(this, "start #%d", this.number);
            Boolean result;
            try {
                new AliasRowMocker(DataSourceBuilderTest.NAME).mock();
                result = true;
                Logger.debug(this, "finish #%d", this.number);
            // @checkstyle IllegalCatch (1 line)
            } catch (Exception ex) {
                result = false;
                Logger.error(this, "fail #%d: %[exception]s", this.number, ex);
            }
            return result;
        }
    }

}
