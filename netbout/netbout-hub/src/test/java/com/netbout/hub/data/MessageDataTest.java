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
package com.netbout.hub.data;

import com.netbout.hub.BoutDtMocker;
import com.netbout.hub.PowerHub;
import com.netbout.hub.PowerHubMocker;
import com.netbout.spi.Urn;
import com.netbout.spi.UrnMocker;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case of {@link MessageData}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class MessageDataTest {

    /**
     * MessageDate can set seen-by status flag.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void savesSeenByFlag() throws Exception {
        final PowerHub hub = new PowerHubMocker().mock();
        final MessageData data = new MessageData(
            hub,
            new Random().nextLong(),
            new BoutDtMocker().mock()
        );
        final Urn identity = new UrnMocker().mock();
        data.addSeenBy(identity);
        Mockito.verify(hub).make("was-message-seen");
        // @checkstyle MultipleStringLiterals (1 line)
        Mockito.verify(hub).make("message-was-seen");
        Mockito.reset(hub);
        MatcherAssert.assertThat("was seen", data.isSeenBy(identity));
        // @checkstyle MultipleStringLiterals (1 line)
        Mockito.verify(hub, Mockito.times(0)).make("message-was-seen");
    }

}
