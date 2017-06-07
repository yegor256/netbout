/**
 * Copyright (c) 2009-2017, netbout.com
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
package com.netbout.client;

import com.netbout.spi.Alias;
import com.netbout.spi.Attachment;
import com.netbout.spi.Attachments;
import com.netbout.spi.Bout;
import com.netbout.spi.Inbox;
import com.netbout.spi.User;
import java.io.ByteArrayInputStream;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;

/**
 * Integration case for {@link RtAttachments}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 2.4
 */
public final class RtAttachmentsITCase {

    /**
     * Netbout rule.
     * @checkstyle VisibilityModifierCheck (3 lines)
     */
    @Rule
    public final transient NbRule rule = new NbRule();

    /**
     * RtAttachments can post and read.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void postsAndReads() throws Exception {
        final User user = this.rule.get();
        final Alias alias = user.aliases().iterate().iterator().next();
        final Inbox inbox = alias.inbox();
        final Bout bout = inbox.bout(inbox.start());
        bout.rename(this.getClass().getName());
        final Attachments attachments = bout.attachments();
        final String name = "test";
        attachments.create(name);
        final Attachment attachment = attachments.get(name);
        final byte[] data = new byte[Byte.MAX_VALUE - 1];
        for (int idx = 0; idx < data.length; ++idx) {
            data[idx] = (byte) idx;
        }
        attachment.write(
            new ByteArrayInputStream(data),
            Attachment.MARKDOWN,
            Long.toString(System.currentTimeMillis())
        );
        MatcherAssert.assertThat(
            IOUtils.toByteArray(attachment.read()),
            Matchers.equalTo(data)
        );
        attachments.delete(name);
    }

    /**
     * RtAttachments can obtain author of an attachment.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void obtainsAuthor() throws Exception {
        final User user = this.rule.get();
        final Alias alias = user.aliases().iterate().iterator().next();
        final Inbox inbox = alias.inbox();
        final Attachments attachments = inbox.bout(inbox.start())
            .attachments();
        final String name = "name";
        attachments.create(name);
        final Attachment attachment = attachments.get(name);
        MatcherAssert.assertThat(
            attachment.author(), Matchers.equalTo(alias.name())
        );
        attachments.delete(name);
    }

    /**
     * RtAttachments can obtain the creation date of an attachment.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void obtainsCreationDate() throws Exception {
        final Alias alias = this.rule
            .get().aliases().iterate().iterator().next();
        final Inbox inbox = alias.inbox();
        final Attachments attachments =
            inbox.bout(inbox.start()).attachments();
        final long before = System.currentTimeMillis();
        final String name = "attach-name";
        attachments.create(name);
        final Attachment attachment = attachments.get(name);
        MatcherAssert.assertThat(
            attachment.date().getTime(), Matchers.greaterThanOrEqualTo(before)
        );
        final long after = System.currentTimeMillis();
        MatcherAssert.assertThat(
            attachment.date().getTime(), Matchers.lessThanOrEqualTo(after)
        );
        attachments.delete(name);
    }

}
