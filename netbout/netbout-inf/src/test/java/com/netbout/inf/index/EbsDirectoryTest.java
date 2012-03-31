/**
 * Copyright (c) 2009-2011, netBout.com
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
package com.netbout.inf.index;

import com.netbout.spi.Urn;
import java.util.concurrent.ConcurrentMap;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case of {@link EbsDirectory}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class EbsDirectoryTest {

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * EbsDirectory can return path to directory.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void returnsPathToFile() throws Exception {
        final EbsDirectory dir = new EbsDirectory(this.temp.newFolder("a"));
        MatcherAssert.assertThat(
            dir.path().exists(),
            Matchers.equalTo(true)
        );
        MatcherAssert.assertThat(
            dir.path().isDirectory(),
            Matchers.equalTo(true)
        );
    }

    /**
     * EbsDirectory can check its mounting status.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void checksMountingStatus() throws Exception {
        final EbsDirectory dir = new EbsDirectory(this.temp.newFolder("b"));
        MatcherAssert.assertThat(
            dir.mounted(),
            Matchers.equalTo(false)
        );
    }

}
