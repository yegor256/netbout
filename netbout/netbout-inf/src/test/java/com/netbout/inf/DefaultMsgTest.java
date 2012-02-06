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

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link DefaultMsg}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class DefaultMsgTest {

    /**
     * DefaultMsg can accept changes.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void acceptsNewPropertiesAndFindsThem() throws Exception {
        final DefaultMsg msg = new DefaultMsg(1L, 1L);
        final String name = "property-name";
        final Long value = 2L;
        msg.put(name, value);
        msg.close();
        MatcherAssert.assertThat("legal prop", msg.has(name, value));
        MatcherAssert.assertThat("illegal value", !msg.has(name, "some value"));
        MatcherAssert.assertThat("absent prop", !msg.has("some name", 1));
    }

    /**
     * DefaultMsg can protect against changes after closing.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = IllegalStateException.class)
    public void rejectsChangesAfterClosing() throws Exception {
        final DefaultMsg msg = new DefaultMsg(1L, 1L);
        msg.close();
        msg.put("name-1", 0);
    }

    /**
     * DefaultMsg can protect against duplicated closing.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = IllegalStateException.class)
    public void rejectsDuplicatedClosing() throws Exception {
        final DefaultMsg msg = new DefaultMsg(1L, 1L);
        msg.close();
        msg.close();
    }

}
