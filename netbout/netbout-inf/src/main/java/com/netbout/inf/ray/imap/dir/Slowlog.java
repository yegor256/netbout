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
package com.netbout.inf.ray.imap.dir;

import com.jcabi.log.Logger;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Slow log for Catalog.
 *
 * <p>Class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class Slowlog extends Backlog {

    /**
     * Public ctor.
     * @param file The file to use
     * @throws IOException If some I/O problem inside
     */
    public Slowlog(final File file) throws IOException {
        super(file);
    }

    /**
     * Convert position to the normal form.
     *
     * <p>If position is negative it means that we should do a full search
     * in slow index, by its UTF value.
     *
     * @param pos Position found in fast index
     * @param value The value
     * @return Normalized position in data file
     * @throws IOException If some I/O problem inside
     */
    public long normalized(final long pos,
        final String value) throws IOException {
        long norm;
        if (pos < 0) {
            norm = Long.valueOf(this.seek(-pos, value));
        } else {
            norm = pos;
        }
        return norm;
    }

    /**
     * Find ref by value, starting with given position.
     * @param pos Position to start with
     * @param value The value to search for
     * @return Reference found
     * @throws IOException If some I/O problem inside
     */
    private String seek(final long pos,
        final String value) throws IOException {
        final RandomAccessFile data =
            new RandomAccessFile(this.file(), "r");
        data.seek(pos);
        String ref;
        while (true) {
            final String val = data.readUTF();
            if (val.equals(Backlog.EOF_MARKER)) {
                throw new IllegalArgumentException(
                    String.format(
                        "value '%s' not found in slow index",
                        value
                    )
                );
            }
            ref = data.readUTF();
            if (val.equals(value)) {
                Logger.debug(
                    this,
                    "#seek(%d, '%[text]s'): found pos #%s in slow search",
                    pos,
                    value,
                    ref
                );
                break;
            }
        }
        data.close();
        return ref;
    }

}
