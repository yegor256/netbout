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
package com.netbout.lite;

import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import com.netbout.spi.Query;
import com.netbout.spi.Urn;
import com.netbout.spi.UrnMocker;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link NetboutLite}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class NetboutLiteTest {

    /**
     * NetboutLite can create an identity.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void createsNewIdentity() throws Exception {
        final Urn urn = new UrnMocker().mock();
        final NetboutLite lite = new NetboutLite();
        MatcherAssert.assertThat(
            lite.login(urn).name(),
            Matchers.equalTo(urn)
        );
    }

    /**
     * NetboutLite can support full cycle of operations.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void supportsFullCycleOfOperations() throws Exception {
        final Urn urn = new UrnMocker().mock();
        final NetboutLite lite = new NetboutLite();
        final Identity self = lite.login(urn);
        final Bout bout = self.bout(self.start().number());
        bout.invite(self.friend(new UrnMocker().mock()));
        MatcherAssert.assertThat(
            new Bout.Smart(bout).participant(urn).name(),
            Matchers.equalTo(urn)
        );
        final Message msg = bout.post("how are you?");
        MatcherAssert.assertThat(
            msg.author().name(),
            Matchers.equalTo(urn)
        );
        MatcherAssert.assertThat(
            self.inbox(new Query.Textual("how")),
            Matchers.<Bout>iterableWithSize(1)
        );
        MatcherAssert.assertThat(
            self.inbox(
                new Query.Textual(
                    "(and (pos 1) (matches 'x') (or (equals $text 'ff')))"
                )
            ),
            Matchers.emptyIterable()
        );
        MatcherAssert.assertThat(
            bout.messages(new Query.Textual("are you?")),
            Matchers.<Message>iterableWithSize(1)
        );
        MatcherAssert.assertThat(
            bout.messages(
                new Query.Textual(
                    "(or (ns '#abc') (pos 5))"
                )
            ),
            Matchers.emptyIterable()
        );
    }

}
