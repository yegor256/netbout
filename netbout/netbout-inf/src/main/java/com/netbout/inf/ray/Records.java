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
 * this code occasionally and without intent to use it, please report this
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
package com.netbout.inf.ray;

import com.jcabi.log.Logger;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;

/**
 * Persistent set of records.
 *
 * <p>This class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
final class Records implements Closeable {

    /**
     * The file to use for records.
     */
    private final transient File file;

    /**
     * Public ctor.
     * @param path Path of the file to use
     * @throws IOException If some IO error
     */
    public Records(final File path) throws IOException {
        this.file = path;
        this.file.getParentFile().mkdirs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        if (this.file.exists()) {
            Logger.debug(
                this,
                "#close(): closed with %d bytes in %s",
                this.file.length(),
                this.file
            );
        } else {
            Logger.debug(this, "#close(): closed without a file");
        }
    }

    /**
     * Restore map from file.
     * @return The map found in the file
     * @throws IOException If some IO error
     */
    public IndexMap restore() throws IOException {
        final IndexMap map = new DefaultIndexMap();
        if (this.file.exists()) {
            InputStream stream;
            try {
                stream = new FileInputStream(this.file);
            } catch (java.io.FileNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
            try {
                final long start = System.currentTimeMillis();
                final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, CharEncoding.UTF_8)
                );
                int count = 0;
                while (true) {
                    final String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    this.parse(line, map);
                    ++count;
                }
                Logger.info(
                    this,
                    // @checkstyle LineLength (1 line)
                    "#restore(): restored %d line(s) from %s (%d bytes) in %[ms]s",
                    count,
                    this.file,
                    this.file.length(),
                    System.currentTimeMillis() - start
                );
            } finally {
                IOUtils.closeQuietly(stream);
            }
        }
        return map;
    }

    /**
     * Record one line to the file.
     * @param args Arguments
     */
    public void add(final Object... args) {
        OutputStream stream;
        try {
            stream = new FileOutputStream(this.file, true);
        } catch (java.io.FileNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
        try {
            final PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(stream, CharEncoding.UTF_8)
            );
            for (Object arg : args) {
                writer.print(
                    URLEncoder.encode(arg.toString(), CharEncoding.UTF_8)
                );
                writer.print(' ');
            }
            writer.println();
            writer.flush();
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * Parse one line.
     * @param line The line to parse
     * @param map The map to save to
     * @throws IOException If some IO error
     * @checkstyle MagicNumber (20 lines)
     */
    private void parse(final String line, final IndexMap map)
        throws IOException {
        final String[] parts = this.decode(line.split(" "));
        if ("touch".equals(parts[0])) {
            map.touch(Long.valueOf(parts[1]));
        } else if ("replace".equals(parts[0])) {
            map.index(parts[1]).replace(Long.valueOf(parts[2]), parts[3]);
        } else if ("add".equals(parts[0])) {
            map.index(parts[1]).add(Long.valueOf(parts[2]), parts[3]);
        } else if ("delete".equals(parts[0])) {
            map.index(parts[1]).delete(Long.valueOf(parts[2]), parts[3]);
        } else if ("clean".equals(parts[0])) {
            map.index(parts[1]).clean(Long.valueOf(parts[2]));
        } else {
            throw new IOException(
                String.format("invalid line format: '%s'", line)
            );
        }
    }

    /**
     * Decode collection of string parts.
     * @param parts Parts to decode
     * @return Decoded parts
     * @throws IOException If some IO error
     */
    private String[] decode(final String[] parts) throws IOException {
        final String[] decoded = new String[parts.length];
        for (int pos = 0; pos < parts.length; ++pos) {
            decoded[pos] = URLDecoder.decode(parts[pos], CharEncoding.UTF_8);
        }
        return decoded;
    }

}
