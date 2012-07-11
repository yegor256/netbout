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
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case of {@link Versions}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class VersionsTest {

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * Versions can create version number.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void createsVersionOfBaseline() throws Exception {
        final File dir = new File(this.temp.newFolder("foo"), "/some/folder");
        final Versions builder = new Versions(dir);
        final String ver = builder.baselined();
        new File(dir, ver).mkdir();
        MatcherAssert.assertThat(builder.baselined(), Matchers.equalTo(ver));
        MatcherAssert.assertThat(
            builder.draft(),
            Matchers.not(Matchers.equalTo(ver))
        );
    }

    /**
     * Versions can create version number.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void createsVersionOfDraft() throws Exception {
        final Versions builder = new Versions(
            new File(this.temp.newFolder("foo-1"), "/some/folder/to/create-2")
        );
        final String ver = builder.draft();
        MatcherAssert.assertThat(
            builder.draft(),
            Matchers.not(Matchers.equalTo(ver))
        );
    }

}
