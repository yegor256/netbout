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
package com.netbout.hub.cron;

import com.jcabi.log.Logger;
import com.jcabi.log.VerboseThreads;
import com.netbout.hub.PowerHub;
import com.netbout.inf.notices.MessagePostedNotice;
import com.netbout.spi.Message;
import com.netbout.spi.Urn;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Indexes messages in INF.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class Indexer extends AbstractCron {

    /**
     * Public ctor.
     * @param hub The hub
     */
    public Indexer(final PowerHub hub) {
        super(hub);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void call() throws Exception {
        final long start = System.nanoTime();
        final Long maximum = this.hub().infinity().maximum();
        final List<Long> numbers = this.hub().make("get-messages-chunk")
            .synchronously()
            .arg(maximum)
            // @checkstyle MagicNumber (1 line)
            .arg(20000L)
            .asDefault(new ArrayList<Long>(0))
            .exec();
        if (!numbers.isEmpty()) {
            Logger.info(
                this,
                "#cron(): %d messages (%d..%d) going to INF now (maximum=%d)",
                numbers.size(),
                numbers.get(0),
                numbers.get(numbers.size() - 1),
                maximum
            );
            this.execute(numbers);
            Logger.info(
                this,
                "#cron(): %d messages (%d..%d) pushed to INF in %[nano]s",
                numbers.size(),
                numbers.get(0),
                numbers.get(numbers.size() - 1),
                System.nanoTime() - start
            );
        }
        return null;
    }

    /**
     * Execute them all in multiple threads.
     * @param numbers Numbers of messages
     * @throws Exception If something goes wrong
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void execute(final List<Long> numbers) throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(
            // @checkstyle MagicNumber (1 line)
            Runtime.getRuntime().availableProcessors() * 4,
            new VerboseThreads(this)
        );
        final Collection<Future<?>> futures =
            new ArrayList<Future<?>>(numbers.size());
        for (final Long number : numbers) {
            futures.add(
                executor.submit(
                    new Callable<Void>() {
                        public Void call() throws Exception {
                            final Message message =
                                Indexer.this.message(number);
                            Indexer.this.hub().infinity().see(
                                new MessagePostedNotice() {
                                    @Override
                                    public Message message() {
                                        return message;
                                    }
                                }
                            );
                            return null;
                        }
                    }
                )
            );
        }
        try {
            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Create a message from its number.
     * @param number Number of message
     * @return The message itself
     * @throws Exception If something wrong with this msg
     */
    private Message message(final Long number) throws Exception {
        final Long bnum = this.hub().make("get-bout-of-message")
            .synchronously()
            .arg(number)
            .exec();
        final List<Urn> dudes = this.hub().make("get-bout-participants")
            .synchronously()
            .arg(bnum)
            .exec();
        if (dudes.isEmpty()) {
            throw new IllegalStateException(
                String.format(
                    "empty list of participants for bout #%d (message #%d)",
                    bnum,
                    number
                )
            );
        }
        return this.hub().identity(dudes.get(0)).bout(bnum).message(number);
    }

}
