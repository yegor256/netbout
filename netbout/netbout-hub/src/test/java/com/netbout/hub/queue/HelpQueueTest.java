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
package com.netbout.hub.queue;

import com.netbout.spi.cpa.CpaHelper;
import com.netbout.spi.cpa.Farm;
import com.netbout.spi.cpa.Operation;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link HelpQueue}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class HelpQueueTest {

    /**
     * Simple synch transaction with a helper.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testSynchronousTransaction() throws Exception {
        HelpQueue.register(new CpaHelper(this.getClass()));
        final String result = HelpQueue.make("simple-translation")
            .priority(HelpQueue.Priority.SYNCHRONOUSLY)
            .arg("test me")
            .asDefault("doesn't work")
            .exec(String.class);
        MatcherAssert.assertThat(result, Matchers.equalTo("XXXX XX"));
    }

    /**
     * List returned should be processed properly.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testTransactionReturningList() throws Exception {
        HelpQueue.register(new CpaHelper(this.getClass()));
        // @checkstyle MagicNumber (1 line)
        final Integer size = new Random().nextInt(20);
        final Long[] list = HelpQueue.make("simple-list")
            .priority(HelpQueue.Priority.SYNCHRONOUSLY)
            .arg(size)
            .exec(Long[].class);
        MatcherAssert.assertThat(list.length, Matchers.equalTo(size));
    }

    @Farm
    public static final class SimpleHelper {
        /**
         * Translate text.
         * @param text The message to translate
         * @return New text to show
         */
        @Operation("simple-translation")
        public String translate(final String text) {
            return text.replaceAll("[a-z]", "X");
        }
        /**
         * Return list.
         * @param size Size of list to return
         * @return The list
         */
        @Operation("simple-list")
        public Long[] list(final Long size) {
            final Long[] list = new Long[size.intValue()];
            for (int pos = 0; pos < size; pos += 1) {
                list[pos] = new Random().nextLong();
            }
            return list;
        }
    }

}
