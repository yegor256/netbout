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
package com.netbout.email;

import com.jcabi.email.Envelope;
import com.jcabi.email.Postman;
import com.netbout.mock.MkBase;
import com.netbout.spi.Alias;
import com.netbout.spi.Bout;
import java.io.IOException;
import org.junit.Test;
import org.mockito.Mockito;
import org.takes.facets.forward.RsFailure;

/**
 * Test case for {@link EmMessages}.
 * @author Dragan Bozanovic (bozanovicdr@gmail.com)
 * @version $Id$
 * @since 2.18
 */
public final class EmMessagesTest {

    /**
     * Can throw a user-friendly exception in case of email
     * delivery failure.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = RsFailure.class)
    public void throwsUserFriendlyExceptionOnFailure() throws Exception {
        final Postman postman = Mockito.mock(Postman.class);
        final MkBase base = new MkBase();
        final Alias alias = new EmAlias(base.randomAlias(), postman);
        final Bout bout = alias.inbox().bout(alias.inbox().start());
        bout.friends().invite(base.randomAlias().name());
        Mockito.doThrow(new IOException()).when(postman)
            .send(Mockito.any(Envelope.class));
        final EmMessages emMessages = new EmMessages(
            bout.messages(),
            postman,
            bout,
            alias.name()
        );
        emMessages.post("how are you?");
    }
}
