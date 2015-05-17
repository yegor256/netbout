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
package com.netbout.dynamo;

import com.jcabi.aspects.Tv;
import com.jcabi.urn.URN;
import com.netbout.spi.Aliases;
import com.netbout.spi.Attachment;
import com.netbout.spi.Attachments;
import com.netbout.spi.Bout;
import com.netbout.spi.Inbox;
import java.io.ByteArrayInputStream;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Integration case for {@link DyAttachment}.
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.8
 */
public final class DyAttachmentITCase {

    /**
     * DyAttachment can create, save and load attachments.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void createsAndLoadsAttachment() throws Exception {
        final String alias = "beatrix";
        final Aliases aliases =
            new DyBase().user(new URN("urn:test:89635")).aliases();
        aliases.add(alias);
        final Inbox inbox = aliases.iterate().iterator().next().inbox();
        final Bout bout = inbox.bout(inbox.start());
        final Attachments attachments = bout.attachments();
        final String name = "test";
        attachments.create(name);
        final Attachment attachment = attachments.get(name);
        final byte[] bytes = new byte[Tv.FORTY * Tv.THOUSAND];
        for (int idx = 0; idx < bytes.length; ++idx) {
            bytes[idx] = (byte) idx;
        }
        final String etag = Long.toString(System.currentTimeMillis());
        attachment.write(
            new ByteArrayInputStream(bytes),
            MediaType.APPLICATION_OCTET_STREAM,
            etag
        );
        MatcherAssert.assertThat(
            IOUtils.toByteArray(attachment.read()),
            Matchers.equalTo(bytes)
        );
        MatcherAssert.assertThat(
            attachments.get(name).etag(),
            Matchers.equalTo(etag)
        );
    }

}
