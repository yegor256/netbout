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
package com.netbout.inf.ray.imap;

import com.netbout.inf.Attribute;
import com.netbout.inf.Stash;
import java.io.Closeable;
import java.io.IOException;

/**
 * Directory with files.
 *
 * <p>Implementation must be thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public interface Directory extends Closeable {

    /**
     * Save numbers for the given attribute.
     * @param attr The attribute
     * @param value The value to set them to
     * @param nums The numbers to save from
     * @throws IOException If some I/O problem inside
     */
    void save(Attribute attr, String value, Numbers nums) throws IOException;

    /**
     * Load numbers for the given attribute.
     * @param attr The attribute
     * @param value The value to set them to
     * @param nums The numbers to load into
     * @throws IOException If some I/O problem inside
     */
    void load(Attribute attr, String value, Numbers nums) throws IOException;

    /**
     * Save reverse for the given attribute.
     * @param attr The attribute
     * @param reverse The reverse to save from
     * @throws IOException If some I/O problem inside
     */
    void save(Attribute attr, Reverse reverse) throws IOException;

    /**
     * Load reverse for the given attribute.
     * @param attr The attribute
     * @param reverse The reverse to load to
     * @throws IOException If some I/O problem inside
     */
    void load(Attribute attr, Reverse reverse) throws IOException;

    /**
     * Baseline existing version (if we loose power right after this operation
     * this version will be loaded after reboot).
     * @throws IOException If some I/O problem inside
     */
    void baseline() throws IOException;

    /**
     * Get stash.
     * @return The stash to use
     * @throws IOException If IO problem insde
     */
    Stash stash() throws IOException;

}
