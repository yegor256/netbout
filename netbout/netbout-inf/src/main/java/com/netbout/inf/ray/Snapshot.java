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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;

/**
 * Snapshot of files.
 *
 * <p>This class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class Snapshot {

    /**
     * The directory.
     */
    private final transient File dir;

    /**
     * The version.
     */
    private final transient String ver;

    /**
     * Public ctor.
     * @param file Directory where files are kept
     * @param version Version
     * @throws IOException If some IO error
     */
    public Snapshot(final File file, final String version) throws IOException {
        this.dir = file;
        this.ver = version;
    }

    /**
     * Get version of it.
     * @return The version
     */
    public String version() {
        return this.ver;
    }

    /**
     * This name is inside?
     * @param name File name
     * @return Yes or no
     */
    public boolean includes(final String name) {
        return name.startsWith(this.ver);
    }

    /**
     * Get file for the central map.
     * @return The file
     * @throws IOException If some IO error
     */
    public File map() throws IOException {
        final File file = new File(
            this.dir,
            String.format("%s-map.txt", this.ver)
        );
        FileUtils.touch(file);
        return file;
    }

    /**
     * Get file for the attribute.
     * @param attr Attribute name
     * @return The file
     * @throws IOException If some IO error
     */
    public File attr(final String attr) throws IOException {
        final File file = new File(
            this.dir,
            String.format("%s-a-%s.txt", this.ver, attr)
        );
        FileUtils.touch(file);
        return file;
    }

    /**
     * Get list of all attributes available here.
     * @return The list of their names
     * @throws IOException If some IO error
     */
    public Collection<String> attrs() throws IOException {
        final Pattern pattern = Pattern.compile(
            String.format("%s-a-(.*?)\\.txt", Pattern.quote(this.ver))
        );
        final Collection<String> names = new LinkedList<String>();
        for (String name : this.dir.list()) {
            final Matcher matcher = pattern.matcher(name);
            if (matcher.matches()) {
                names.add(matcher.group(1));
            }
        }
        return names;
    }

}
