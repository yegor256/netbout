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
package com.netbout.inf.ray.imap.dir;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case of {@link Lock}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class LockTest {

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * Lock can lock a directory and release lock later.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void locksDirectoryAndReleases() throws Exception {
        final File dir = new File(this.temp.newFolder("foo"), "/boom/a");
        Lock lock = new Lock(dir);
        lock.close();
        lock = new Lock(dir);
        lock.close();
    }

    /**
     * Lock can prevent against duplicate instances.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = java.io.IOException.class)
    public void preventsDuplicateInstances() throws Exception {
        final File dir = new File(this.temp.newFolder("foo-2"), "/boom/x");
        new Lock(dir);
        new Lock(dir);
    }

    /**
     * Lock can delete all files in the directory.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void deletesAllFilesInDirectory() throws Exception {
        final File dir = this.temp.newFolder("some-dir-1");
        final File file = new File(dir, "some-file.txt");
        FileUtils.touch(file);
        MatcherAssert.assertThat(file.exists(), Matchers.is(true));
        Lock lock = new Lock(dir);
        lock.clear();
        lock.close();
        MatcherAssert.assertThat(file.exists(), Matchers.is(false));
    }

}
