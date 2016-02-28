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
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
@SuppressWarnings("PMD.ExcessiveImports")
final class TkAttach implements Take {

    /**
     * Pattern to look for file name.
     */
    private static final Pattern FILE_NAME_PATTERN = Pattern.compile(
        "(.*)(name=\"file\")(.*)(filename=\"(.*)\")(.*)"
    );

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
        final RqMultipart.Smart form = new RqMultipart.Smart(
            new RqMultipart.Base(req)
        );
        final Request file = form.single("file");
        final String name = this.filename(form, file);
        final Bout bout = new RqBout(this.base, req).bout();
        final File temp = File.createTempFile("netbout", "bin");
        try {
            this.copyAndValidate(file, temp, name);
            final StringBuilder msg = new StringBuilder(Tv.HUNDRED);
            if (new Attachments.Search(bout.attachments()).exists(name)) {
                msg.append(
                    String.format("attachment \"%s\" overwritten", name)
                );
            } else {
                bout.attachments().create(name);
                msg.append(String.format("attachment \"%s\" uploaded", name));
            }
            final String ctype = TkAttach.ctype(temp);
            msg.append(" (").append(temp.length())
                .append(" bytes, ").append(ctype).append(')');
            try (InputStream is = new FileInputStream(temp)) {
                bout.attachments().get(name).write(
                    is,
                    ctype, Long.toString(System.currentTimeMillis())
                );
            }
            bout.messages().post(msg.toString());
            throw new RsForward(new RsFlash(msg.toString()));
        } catch (final Attachment.TooBigException
            | Attachment.BrokenContentException
            | Attachments.InvalidNameException ex) {
            if (new Attachments.Search(bout.attachments()).exists(name)) {
                bout.attachments().delete(name);
            }
            throw new RsFailure(ex);
        } finally {
            FileUtils.forceDelete(temp);
        }
    }

    /**
     * Reads the filename either from the name field or from the uploaded file.
     * @todo #865:30min write a test for this method. It should make sure that
     *  if there is a name part in the form and it is not empty then it is
     *  taken and if not, the name of the uploaded file is used.
     * @param form From
     * @param file File
     * @return String name
     * @throws IOException If fails
     */
    private String filename(final RqMultipart.Smart form, final Request file)
        throws IOException {
        final String name;
        final Iterable<Request> requests = form.part("name");
        if (requests.iterator().hasNext()) {
            name = IOUtils.toString(
                requests.iterator().next().body(),
                StandardCharsets.UTF_8
            );
        } else {
            name = "";
        }
        final String filename;
        if (StringUtils.isBlank(name)) {
            filename = this.nameFromFile(file);
        } else {
            filename = name;
        }
        return filename;
    }

    /**
     * Extracts name from file part.
     * @param file File
     * @return String name
     * @throws IOException If fails
     */
    private String nameFromFile(final Request file) throws IOException {
        final Matcher matcher = TkAttach.FILE_NAME_PATTERN.matcher(
            new RqHeaders.Smart(
                new RqHeaders.Base(file)
            ).single("Content-Disposition")
        );
        if (!matcher.find()) {
            throw new RsFailure("Filename was not provided");
        }
        // @checkstyle MagicNumberCheck (1 line)
        return URLDecoder.decode(matcher.group(5), CharEncoding.UTF_8);
    }

    /**
     * Copy the multipart data to a file and validate it.
     * @param src Multipart request
     * @param dst File
     * @param name Filename
     * @throws IOException If copy or validate fails
     */
    private void copyAndValidate(final Request src, final File dst,
        final String name) throws IOException {
        Files.copy(
            src.body(),
            dst.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        );
        this.validate(dst, name);
    }

    /**
     * Checks some limitations on attachment.
     * @param attach Temportary file with attachment
     * @param name Attachment name
     * @throws IOException If attachment violates limitations
     */
    private void validate(final File attach, final String name)
        throws IOException {
        if (attach.length() == 0) {
            throw new Attachment.BrokenContentException(
                String.format(
                    "content of attachment \"%s\" can't be empty",
                    name
                )
            );
        }
        if (attach.length() > Tv.TEN * Tv.MILLION) {
            throw new Attachment.TooBigException(
                String.format(
                    "attachment \"%s\" is too big, 10Mb is the maximum size",
                    name
                )
            );
        }
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
