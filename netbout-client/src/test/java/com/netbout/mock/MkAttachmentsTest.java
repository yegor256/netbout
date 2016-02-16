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

import com.jcabi.aspects.Tv;
import com.netbout.spi.Attachment;
import com.netbout.spi.Attachments;
import com.netbout.spi.Bout;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link MkAttachments}.
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.4
 */
public final class MkAttachmentsTest {

    /**
     * MkAttachments can upload and download attachments.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void uploadsAndDownloads() throws Exception {
        final Bout bout = new MkBase().randomBout();
        final Attachments attachments = bout.attachments();
        final String name = "test-name";
        attachments.create(name);
        final Attachment attachment = attachments.get(name);
        attachment.write(
            IOUtils.toInputStream("hey \u20ac", StandardCharsets.UTF_8),
            "text/plain",
            Long.toString(System.currentTimeMillis())
        );
        MatcherAssert.assertThat(
            IOUtils.toString(attachment.read(), StandardCharsets.UTF_8),
            Matchers.containsString("\u20ac")
        );
        MatcherAssert.assertThat(
            attachment.ctype(),
            Matchers.containsString("/plain")
        );
    }

    /**
     * MkAttachments can change update attribute on Bout.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void changesUpdateAttribute() throws Exception {
        final Bout bout = new MkBase().randomBout();
        final Attachments attachments = bout.attachments();
        final Long last = bout.updated().getTime();
        final String name = "att-name";
        attachments.create(name);
        final Attachment attachment = attachments.get(name);
        attachment.write(
            IOUtils.toInputStream("<root/>", StandardCharsets.UTF_8),
            "text/xml",
            Long.toString(System.currentTimeMillis())
        );
        Thread.sleep(Tv.HUNDRED);
        MatcherAssert.assertThat(
            bout.updated().getTime(), Matchers.greaterThan(last)
        );
    }
}
