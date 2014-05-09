/**
 * Copyright (c) 2009-2014, netbout.com
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

import com.jcabi.aspects.Immutable;
import com.jcabi.http.Request;
import com.jcabi.http.response.XmlResponse;
import com.netbout.spi.Attachment;
import java.io.IOException;
import java.io.InputStream;

/**
 * REST attachment.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 2.0
 */
@Immutable
final class RtAttachment implements Attachment {

    /**
     * Request to use.
     */
    private final transient Request request;

    /**
     * Its name.
     */
    private final transient String attachment;

    /**
     * Public ctor.
     * @param req Request to use
     * @param name Its name
     */
    RtAttachment(final Request req, final String name) {
        this.request = req;
        this.attachment = name;
    }

    @Override
    public String name() {
        return this.attachment;
    }

    @Override
    public String ctype() throws IOException {
        return this.request.fetch()
            .as(XmlResponse.class)
            .xml()
            .xpath(this.xpath("ctype/text()"))
            .get(0);
    }

    @Override
    public InputStream read() throws IOException {
        throw new UnsupportedOperationException("#read()");
    }

    @Override
    public void write(final InputStream stream, final String ctype)
        throws IOException {
        throw new UnsupportedOperationException("#write()");
    }

    /**
     * Xpath of the attachment in the page.
     * @param path Path to append
     * @return XPath
     */
    private String xpath(final String path) {
        return String.format(
            "/page/bout/attachments/attachment[name='%s']/%s",
            this.attachment, path
        );
    }
}
