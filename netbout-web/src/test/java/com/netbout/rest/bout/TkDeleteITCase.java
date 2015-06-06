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
package com.netbout.rest.bout;

import com.netbout.client.RtUser;
import com.netbout.spi.Alias;
import com.netbout.spi.Attachments;
import com.netbout.spi.Bout;
import com.netbout.spi.User;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Integration case for {@link TkDelete}.
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.15
 */
public final class TkDeleteITCase {

    /**
     * Home page of Tomcat.
     */
    private static final String HOME = System.getProperty("takes.home");

    /**
     * TkDelete can delete an attachment.
     * @throws Exception If there is some problem inside
     * @todo #662 This test is failing after upgrading to Takes 0.20. See #669
     */
    @Test
    @Ignore
    public void deletesAttachments() throws Exception {
        final User user = new RtUser(URI.create(TkDeleteITCase.HOME), "");
        final Alias alias = user.aliases().iterate().iterator().next();
        final Bout bout = alias.inbox().bout(alias.inbox().start());
        final Attachments attachments = bout.attachments();
        final String name = "test";
        attachments.create(name);
        MatcherAssert.assertThat(
            attachments.iterate().iterator().next().name(),
            Matchers.equalTo(name)
        );
        attachments.delete(name);
        MatcherAssert.assertThat(
            attachments.iterate(),
            Matchers.emptyIterable()
        );
    }

}
