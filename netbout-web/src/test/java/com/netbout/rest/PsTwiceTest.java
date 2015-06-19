/**
 * Copyright (c) 2009-2015, netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netbout Inc. located at www.netbout.com.
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
package com.netbout.rest;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.takes.facets.auth.Identity;
import org.takes.facets.auth.PsFake;
import org.takes.facets.auth.PsLogout;
import org.takes.rq.RqFake;
import org.takes.rs.RsEmpty;

/**
 * Test case for {@link com.netbout.rest.PsTwice}.
 * @author Eugene Kondrashev (eugene.kondrashev@gmai.com)
 * @version $Id$
 * @since 2.15.1
 */
public final class PsTwiceTest {

    /**
     * PsTwice can return second identity.
     * @throws Exception if some problems inside
     */
    @Test
    public void returnsSecondIdentity() throws Exception {
        MatcherAssert.assertThat(
            new PsTwice(
                new PsFake(true),
                new PsLogout()
            ).enter(new RqFake()).get(),
            Matchers.is(Identity.ANONYMOUS)
        );
    }

    /**
     * PsTwice can return empty identity.
     * @throws Exception if some problems inside
     */
    @Test
    public void returnsEmptyIdentity() throws Exception {
        MatcherAssert.assertThat(
            new PsTwice(
                new PsFake(false),
                new PsLogout()
            ).enter(new RqFake()).has(),
            Matchers.equalTo(false)
        );
    }

    /**
     * PsTwice can return correct response on exit.
     * @throws Exception if some problems inside
     */
    @Test
    public void returnsCorrectResponseOnExit() throws Exception {
        MatcherAssert.assertThat(
            new PsTwice(
                new PsFake(true),
                new PsLogout()
            ).exit(new RsEmpty(), Identity.ANONYMOUS)
                .head().iterator().next(),
            Matchers.containsString("HTTP/1.1 200 O")
        );
    }
}
