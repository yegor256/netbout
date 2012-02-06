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

import java.util.Arrays;
import java.util.Iterator;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link LazyBouts}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class LazyBoutsTest {

    /**
     * LazyBouts can find bouts.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsBoutsInStreamOfMessages() throws Exception {
        final Heap heap = new Heap();
        heap.put(1L, new DefaultMsg(1L, 2L));
        heap.put(2L, new DefaultMsg(2L, 2L));
        final Iterable<Long> messages = Arrays.asList(new Long[] {1L});
        final Iterable<Long> bouts = new LazyBouts(heap, messages);
        MatcherAssert.assertThat(
            bouts,
            Matchers.allOf(
                (Matcher) Matchers.iterableWithSize(1),
                (Matcher) Matchers.hasItem(Matchers.equalTo(2L))
            )
        );
    }

    /**
     * LazyBouts can return correct value of {@code hasNext}.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void returnsHasNextCorrectly() throws Exception {
        final Heap heap = new Heap();
        heap.put(1L, new DefaultMsg(1L, 2L));
        heap.put(2L, new DefaultMsg(2L, 2L));
        final Iterable<Long> messages = Arrays.asList(new Long[] {1L});
        final Iterator<Long> iter = new LazyBouts(heap, messages).iterator();
        MatcherAssert.assertThat("has an item", iter.hasNext());
        MatcherAssert.assertThat("still has it", iter.hasNext());
    }

    /**
     * LazyBouts can return correct value of {@code hasNext} when no input.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void returnsHasNextCorrectlyOnEmptyInput() throws Exception {
        final Iterator<Long> iter = new LazyBouts(
            new Heap(), Arrays.asList(new Long[] {})
        ).iterator();
        MatcherAssert.assertThat("no items there", !iter.hasNext());
    }

    /**
     * LazyBouts can return correct value of {@code next()}.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void returnsNextValueCorrectly() throws Exception {
        final Heap heap = new Heap();
        heap.put(2L, new DefaultMsg(2L, 2L));
        heap.put(1L, new DefaultMsg(1L, 2L));
        final Iterable<Long> messages = Arrays.asList(new Long[] {1L});
        final Iterator<Long> iter = new LazyBouts(heap, messages).iterator();
        MatcherAssert.assertThat(iter.next(), Matchers.equalTo(2L));
        MatcherAssert.assertThat("no more items there", !iter.hasNext());
    }

    /**
     * LazyBouts throws exception on incorrect call to {@code next()}.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = java.util.NoSuchElementException.class)
    public void throwsWhenIteratorIsEmpty() throws Exception {
        new LazyBouts(new Heap(), Arrays.asList(new Long[] {}))
            .iterator().next();
    }

}
