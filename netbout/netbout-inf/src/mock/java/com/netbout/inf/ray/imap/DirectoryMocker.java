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

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Mocker of {@link Directory}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class DirectoryMocker {

    /**
     * The randomizer.
     */
    private final transient Random random = new SecureRandom();

    /**
     * The object.
     */
    private final transient Directory directory;

    /**
     * Total number of messages.
     */
    private transient int maximum;

    /**
     * Public ctor.
     * @param dir The directory to work with
     * @throws IOException If something wrong inside
     */
    public DirectoryMocker(final File dir) throws IOException {
        this.directory = new DefaultDirectory(dir);
    }

    /**
     * Total amount of messages to have there.
     * @param max Total number of messages
     * @return This object
     * @throws IOException If something wrong inside
     */
    public DirectoryMocker withMaximum(final int max) throws IOException {
        this.maximum = max;
        return this;
    }

    /**
     * Total amount of bouts to have there.
     * @param bouts Total number of bouts
     * @param max Maximum number of messages per bout
     * @return This object
     * @throws IOException If something wrong inside
     */
    public DirectoryMocker withBouts(final int bouts, final int max)
        throws IOException {
        return this;
    }

    /**
     * With this attribute.
     * @param name Name of attribute
     * @param prefix The prefix for values
     * @param num Number of letters to add to prefix
     * @return This object
     * @throws IOException If something wrong inside
     */
    public DirectoryMocker withAttr(final String name, final String prefix,
        final int num) throws IOException {
        return this;
    }

    /**
     * Build it.
     * @return The directory
     * @throws IOException If something wrong inside
     */
    public Directory mock() throws IOException {
        assert this.maximum != 0;
        assert this.random != null;
        this.directory.baseline();
        return this.directory;
    }

}
