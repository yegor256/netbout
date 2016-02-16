/**
 * Copyright (c) 2009-2016, netbout.com
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
package com.netbout.mock;

import com.jcabi.urn.URN;
import com.netbout.spi.Alias;
import com.netbout.spi.Aliases;
import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import java.io.IOException;
import java.security.SecureRandom;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link MkInbox}.
 * @author Lautaro Cozzani (lautaromail@gmail.com)
 * @version $Id$
 * @since 2.15
 */
public class MkInboxTest {

    /**
     * Test start inbox and send invite to current user.
     * @throws IOException If SQL fails
     */
    @Test
    public final void testStart() throws IOException {
        final String name = "current-name";
        final Sql sql = new H2Sql();
        final Aliases aliases = new MkUser(
            sql,
            URN.create(
                String.format(
                    "urn:test:%d",
                    new SecureRandom().nextInt(Integer.MAX_VALUE)
                )
            )
        ).aliases();
        aliases.add(name);
        final Alias alias = aliases.iterate().iterator().next();
        alias.email(String.format("%s@example.com", alias.name()));
        final MkInbox inbox = new MkInbox(sql, name);
        final Bout bout = inbox.bout(inbox.start());
        MatcherAssert.assertThat(
            bout.friends().iterate(),
            Matchers.hasItem(new Friend.HasAlias(Matchers.is(name)))
        );
    }
}
