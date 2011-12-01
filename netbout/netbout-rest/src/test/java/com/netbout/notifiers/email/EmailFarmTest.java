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
package com.netbout.notifiers.email;

import com.netbout.hub.Hub;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link EmailFarm}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class EmailFarmTest {

    /**
     * Check emails validation.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testValidEmailFormatsCheck() throws Exception {
        final EmailFarm farm = new EmailFarm();
        final String[] valid = new String[] {
            "test.me-now+1@example.com.ua",
            "test1_88@alpha-beta-gamma.net.kz",
            "test@example.com",
        };
        for (String email : valid) {
            MatcherAssert.assertThat(
                farm.canNotifyIdentity(email),
                Matchers.equalTo(Boolean.TRUE)
            );
        }
        final String[] invalid = new String[] {
            "tes t@example.com.ua",
            "7274562",
            "test-that-doesn't-work",
        };
        for (String email : invalid) {
            MatcherAssert.assertThat(
                farm.canNotifyIdentity(email),
                Matchers.nullValue()
            );
        }
    }

    // /**
    //  * Test email sending.
    //  * @throws Exception If there is some problem inside
    //  */
    // @Test
    // public void testEmailSending() throws Exception {
    //     final Identity identity = HubEntry.user("temp")
    //         .identity("nb:test@example.com");
    //     final Bout bout = identity.start();
    //     final Message msg = bout.post("Hello, how are you?");
    //     bout.post("Should work fine with\nmulti-line messages");
    //     bout.post("And this one should work");
    //     final EmailFarm farm = new EmailFarm();
    //     farm.notifyBoutParticipants(bout.number(), identity.name(), msg.date());
    // }

}
