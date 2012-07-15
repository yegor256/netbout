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

import com.netbout.inf.ray.MemRay;
import java.util.Iterator;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test case of {@link LazyMessages}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class LazyMessagesTest {

    /**
     * LazyMessages can fetch messages from predicate.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsMessagesInStreamOfMsgs() throws Exception {
        final Ray ray = new RayMocker().mock();
        final Term term = new TermMocker().mock();
        final Iterable<Long> msgs = new LazyMessages(ray, term);
        MatcherAssert.assertThat(msgs, Matchers.notNullValue());
    }

    /**
     * LazyMessages can stop at the end of cursor.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void stopsIteratorOnEndOfCursor() throws Exception {
        final Ray ray = new MemRay(new FolderMocker().mock().path());
        final Term term = ray.builder().never();
        final Iterable<Long> msgs = new LazyMessages(ray, term);
        final Iterator<Long> iterator = msgs.iterator();
        MatcherAssert.assertThat(iterator.hasNext(), Matchers.is(false));
        try {
            iterator.next();
            Assert.fail("exception expected");
        } catch (java.util.NoSuchElementException ex) {
            MatcherAssert.assertThat(iterator.hasNext(), Matchers.is(false));
        }
    }

}
