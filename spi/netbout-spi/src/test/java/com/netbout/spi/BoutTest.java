/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi;

import java.util.Date;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Bout} and {@link BoutMocker}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class BoutTest {

    /**
     * BoutMocker can assign title to the bout.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void canHaveATitleMocked() throws Exception {
        final String title = "some title";
        final Bout bout = new BoutMocker().titledAs(title).mock();
        MatcherAssert.assertThat(bout.title(), Matchers.equalTo(title));
    }

    /**
     * BoutMocker sets bout number automatically.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void setsBoutNumberByDefault() throws Exception {
        final Bout bout = new BoutMocker().mock();
        MatcherAssert.assertThat(bout.number(), Matchers.greaterThan(0L));
    }

    /**
     * BoutMocker sets date automatically.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void setsBoutDateByDefault() throws Exception {
        final Bout bout = new BoutMocker().mock();
        MatcherAssert.assertThat(bout.date(), Matchers.notNullValue());
    }

    /**
     * BoutMocker can add messages to bout by default.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void addsBoutMessageByDefault() throws Exception {
        final Bout bout = new BoutMocker().mock();
        MatcherAssert.assertThat(
            bout.messages(new Query.Textual("")),
            Matchers.not(Matchers.<Message>emptyIterable())
        );
        MatcherAssert.assertThat(bout.message(0L), Matchers.notNullValue());
    }

    /**
     * BoutMocker can assign collection of participants.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void canAssignParticipantsToBout() throws Exception {
        final Bout bout = new BoutMocker()
            .withParticipant(new UrnMocker().mock())
            .withParticipant(new UrnMocker().mock())
            .mock();
        MatcherAssert.assertThat(
            bout.participants().size(),
            Matchers.equalTo(2)
        );
    }

    /**
     * BoutMocker can return given message on pre-defined query.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void mocksMessageByQueryText() throws Exception {
        final Bout bout = new BoutMocker()
            .messageOn("foo", "hello!")
            .mock();
        MatcherAssert.assertThat(
            bout.messages(
                new Query.Textual("foo is inside")
            ).iterator().next().text(),
            Matchers.containsString("hello")
        );
    }

    /**
     * Bout.Smart can calculate the date of a bout.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void calculatesDateOfBout() throws Exception {
        final Date bdate = new Date();
        final Date mdate = new Date(bdate.getTime() + 1);
        final Bout bout = new BoutMocker()
            .withDate(bdate)
            .withMessage(new MessageMocker().withDate(mdate).mock())
            .mock();
        MatcherAssert.assertThat(
            new Bout.Smart(bout).updated(),
            Matchers.equalTo(mdate)
        );
    }

}
