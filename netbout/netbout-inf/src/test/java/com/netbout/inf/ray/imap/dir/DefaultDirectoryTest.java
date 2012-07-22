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

import com.netbout.inf.Attribute;
import com.netbout.inf.MsgMocker;
import com.netbout.inf.ray.imap.Directory;
import com.netbout.inf.ray.imap.Numbers;
import com.netbout.inf.ray.imap.Reverse;
import java.io.File;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case of {@link DefaultDirectory}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class DefaultDirectoryTest {

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * DefaultDirectory can save numbers to file and restore them back.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void savesAndRestoresNumbers() throws Exception {
        final Directory dir = new DefaultDirectory(
            new File(this.temp.newFolder("foo"), "/some/directory")
        );
        final Numbers numbers = new FastNumbers();
        final long msg = MsgMocker.number();
        numbers.add(msg);
        numbers.add(msg - 1);
        final Attribute attr = new Attribute("some-attr");
        final String value = "some value to use";
        dir.save(attr, value, numbers);
        dir.baseline();
        final Numbers restored = new FastNumbers();
        dir.load(attr, value, restored);
        MatcherAssert.assertThat(restored.next(msg), Matchers.equalTo(msg - 1));
    }

    /**
     * DefaultDirectory can save reverse to file and restore them back.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void savesAndRestoresReverse() throws Exception {
        final Directory dir = new DefaultDirectory(
            this.temp.newFolder("foo-2")
        );
        final Reverse reverse = new SimpleReverse();
        final long msg = MsgMocker.number();
        final String value = "some value 2, \u0433";
        reverse.put(msg, value);
        final Attribute attr = new Attribute("some-attr-2");
        dir.save(attr, reverse);
        dir.baseline();
        final Reverse restored = new SimpleReverse();
        dir.load(attr, restored);
        MatcherAssert.assertThat(restored.get(msg), Matchers.equalTo(value));
    }

}
