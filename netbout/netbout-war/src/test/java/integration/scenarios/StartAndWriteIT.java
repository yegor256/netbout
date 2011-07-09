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
 * incident to the author by email: privacy@netbout.com.
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
package integration.scenarios;

import org.junit.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @todo #107 This test doesn't work because functionality is not
 *       implemented yet.
 */
@Ignore
public final class StartAndWriteIT {

    /**
     * This user should exist in the database before the test case
     * is started.
     */
    private static final Long ROOT_USER = 1L;

    private static final String ROOT_PWD = "secret";

    @Test
    public void testFullCycle() throws Exception {
        // let's start a new bout
        final Session starter = new Session();
        starter.login(this.ROOT_USER, this.ROOT_PWD);
        final Long bout = starter.startBout("Let's talk..");
        starter.sendMessage(bout, "Hey!");
        final String inviteUrl = starter.invite(bout, "john@example.com");

        // let's respond to the invitation
        final Session responder = new Session();
        assertThat(responder.acceptInvitation(inviteUrl), equalTo(bout));
        final String message = "Glad to see you!";
        responder.sendMessage(bout, message);

        // let's read the response
        assertThat(starter.readRecent(bout), equalTo(message));
    }

}
