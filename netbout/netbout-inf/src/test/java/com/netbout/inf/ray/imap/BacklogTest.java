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
package com.netbout.inf.ray.imap;

import java.util.Iterator;
import org.apache.commons.collections.IteratorUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case of {@link Backlog}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class BacklogTest {

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * Backlog can register value and find it laters.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void registersValuesAndFindsThen() throws Exception {
        final Backlog backlog = new Backlog(this.temp.newFile("backlog.txt"));
        final String value = "some value to use, \u0433";
        final String ref = "some reference to use, \u0433";
        backlog.add(new Backlog.Item(value, ref));
        backlog.add(new Backlog.Item("abc", ref));
        backlog.add(new Backlog.Item("foo", "bar"));
        final Iterator<Backlog.Item> iterator = backlog.iterator();
        MatcherAssert.assertThat(iterator.hasNext(), Matchers.is(true));
        MatcherAssert.assertThat(iterator.hasNext(), Matchers.is(true));
        MatcherAssert.assertThat(
            iterator.next().value(),
            Matchers.equalTo(value)
        );
        MatcherAssert.assertThat(iterator.hasNext(), Matchers.is(true));
        MatcherAssert.assertThat(
            IteratorUtils.toList(backlog.iterator()).size(),
            Matchers.greaterThan(2)
        );
    }

    /**
     * Backlog can have non-negative size with empty content.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void hasPositiveSizeWithEmptyContent() throws Exception {
        final Backlog backlog = new Backlog(this.temp.newFile("backlog-2.txt"));
        final long pos = backlog.add(new Backlog.Item("foo-2", "bar-2"));
        MatcherAssert.assertThat(pos, Matchers.greaterThan(0L));
    }

}
