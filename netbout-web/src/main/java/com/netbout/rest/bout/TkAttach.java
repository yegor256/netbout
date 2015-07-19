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

import com.jcabi.aspects.Tv;
import com.netbout.spi.Attachment;
import com.netbout.spi.Attachments;
import com.netbout.spi.Base;
import com.netbout.spi.Bout;
import eu.medsea.mimeutil.MimeUtil;
import eu.medsea.mimeutil.detector.MagicMimeMimeDetector;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.flash.RsFlash;
import org.takes.facets.forward.RsFailure;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqHeaders;
import org.takes.rq.RqMultipart;

/**
 * Attach.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.14
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class TkAttach implements Take {

    /**
     * Pattern to look for file name.
     */
    private static final Pattern FILE_NAME_PATTERN = Pattern.compile(
            "(.*)(name=\"file\")(.*)(filename=\"(.*)\")(.*)"
    );

    /**
     * Position of the filename on the RegEx pattern.
     */
    private static final int FILE_NAME_POS = 5;

    /**
     * Base.
     */
    private final transient Base base;

    static {
        MimeUtil.registerMimeDetector(
            MagicMimeMimeDetector.class.getCanonicalName()
        );
    }

    /**
     * Ctor.
     * @param bse Base
     */
    TkAttach(final Base bse) {
        this.base = bse;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final Request file = new RqMultipart.Smart(
            new RqMultipart.Base(req)
        ).single("file");
        final Matcher matcher = FILE_NAME_PATTERN.matcher(
            new RqHeaders.Smart(
                new RqHeaders.Base(file)
            ).single("Content-Disposition")
        );
        if (!matcher.find()) {
            throw new RsFailure("Filename was not provided");
        }
        final String name = URLDecoder.decode(
            matcher.group(FILE_NAME_POS),
            CharEncoding.UTF_8
        );
        final File temp = File.createTempFile("netbout", "bin");
        IOUtils.copy(file.body(), new FileOutputStream(temp));
        final Bout bout = new RqBout(this.base, req).bout();
        final StringBuilder msg = new StringBuilder(Tv.HUNDRED);
        if (new Attachments.Search(bout.attachments()).exists(name)) {
            msg.append(String.format("attachment \"%s\" overwritten", name));
        } else {
            try {
                bout.attachments().create(name);
            } catch (final Attachments.InvalidNameException ex) {
                throw new RsFailure(ex);
            }
            msg.append(String.format("attachment \"%s\" uploaded", name));
        }
        final String ctype = TkAttach.ctype(temp);
        msg.append(" (").append(temp.length())
            .append(" bytes, ").append(ctype).append(')');
        try {
            bout.attachments().get(name).write(
                new FileInputStream(temp),
                ctype, Long.toString(System.currentTimeMillis())
            );
        } catch (final Attachment.TooBigException
            | Attachment.BrokenContentException ex) {
            throw new RsFailure(ex);
        }
        FileUtils.forceDelete(temp);
        throw new RsForward(new RsFlash(msg.toString()));
    }

    /**
     * Get CType of file.
     * @param file File
     * @return MIME type
     */
    private static String ctype(final File file) {
        final Collection<?> ctypes = MimeUtil.getMimeTypes(file);
        final String ctype;
        if (ctypes.isEmpty()) {
            ctype = "application/octet-stream";
        } else {
            ctype = ctypes.iterator().next().toString();
        }
        return ctype;
    }

}
