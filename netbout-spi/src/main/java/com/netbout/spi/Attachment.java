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
package com.netbout.spi;

import com.jcabi.aspects.Immutable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Attachment.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.0
 */
@Immutable
public interface Attachment {

    /**
     * Special content type.
     */
    String MARKDOWN = "text/x-markdown";

    /**
     * Its name.
     * @return Name of it
     * @throws IOException If fails
     */
    String name() throws IOException;

    /**
     * Get its MIME content type.
     * @return Content type
     * @throws IOException If fails
     */
    String ctype() throws IOException;

    /**
     * Get its etag (up to 256 characters).
     * @return ETag or empty string if not yet set
     * @throws IOException If fails
     */
    String etag() throws IOException;

    /**
     * Is it unseen?
     * @return TRUE if it's unseen
     * @throws IOException If fails
     */
    boolean unseen() throws IOException;

    /**
     * When it was created.
     * @return The date of creation
     * @throws IOException If fails
     */
    Date date() throws IOException;

    /**
     * Author of it.
     * @return The author
     * @throws IOException If fails
     */
    String author() throws IOException;

    /**
     * Read content.
     * @return Content
     * @throws IOException If fails
     */
    InputStream read() throws IOException;

    /**
     * Write content (don't touch it if etag is the same as before).
     * @param stream Stream with content
     * @param ctype MIME content type
     * @param etag New ETag (up to 256 characters)
     * @throws IOException If fails
     * @since 2.11
     */
    void write(InputStream stream, String ctype, String etag)
        throws IOException;

    /**
     * Attachment is too big.
     */
    final class TooBigException extends IOException {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = -6379382683897037014L;
        /**
         * Ctor.
         * @param cause Cause of the problem
         */
        public TooBigException(final String cause) {
            super(cause);
        }
    }

    /**
     * Attachment content is not valid.
     */
    final class BrokenContentException extends IOException {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = -6379382653897037014L;
        /**
         * Ctor.
         * @param cause Cause of the problem
         */
        public BrokenContentException(final String cause) {
            super(cause);
        }
    }

}
