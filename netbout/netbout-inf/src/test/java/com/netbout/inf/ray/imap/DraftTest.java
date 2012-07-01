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
import com.netbout.inf.MsgMocker;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.TreeSet;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case of {@link Draft}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class DraftTest {

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * Draft can create file names.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void createsFileNames() throws Exception {
        final Draft draft = new Draft(
            new Lock(new File(this.temp.newFolder("foo"), "/some/folder"))
        );
        final Attribute attr = new Attribute("some-name");
        MatcherAssert.assertThat(draft.numbers(attr), Matchers.notNullValue());
        MatcherAssert.assertThat(draft.reverse(attr), Matchers.notNullValue());
    }

    /**
     * Draft can baseline itself to a baseline, with reverse inside.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void baselinesItselfToBaselineWithReverse() throws Exception {
        final File dir = this.temp.newFolder("foo-3");
        final Attribute attr = new Attribute("boom");
        final Draft draft = new Draft(new Lock(new File(dir, "draft")));
        FileUtils.writeStringToFile(draft.reverse(attr), "reverse-hi!");
        final Baseline src = new Baseline(new Lock(new File(dir, "src")));
        FileUtils.writeStringToFile(src.reverse(attr), "previous value");
        final Baseline dest = new Baseline(new Lock(new File(dir, "dest")));
        draft.baseline(dest, src);
        MatcherAssert.assertThat(
            FileUtils.readFileToString(dest.reverse(attr)),
            Matchers.endsWith("-hi!")
        );
    }

    /**
     * Draft can baseline itself to a baseline, with numbers inside.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void baselinesItselfToBaselineWithNumbers() throws Exception {
        final File dir = this.temp.newFolder("foo-4");
        final Attribute attr = new Attribute("boom-boom-boom");
        final String value = "some data \u0433";
        final Draft draft = new Draft(new Lock(new File(dir, "draft")));
        final File file = draft.numbers(attr);
        draft.backlog(attr).add(
            new Backlog.Item(
                value,
                FilenameUtils.getName(file.getPath())
            )
        );
        final long msg = MsgMocker.number();
        DraftTest.save(file, msg);
        final Baseline src = new Baseline(new Lock(new File(dir, "src")));
        DraftTest.save(src.data(attr), msg + 1, msg - 1);
        src.catalog(attr).create(
            Arrays.asList(new Catalog.Item(value, 0L)).iterator()
        );
        final Baseline dest = new Baseline(
            new Lock(this.temp.newFolder("foo-5"))
        );
        draft.baseline(dest, src);
        draft.close();
        src.close();
        final long pos = dest.catalog(attr).seek(value);
        final RandomAccessFile data =
            new RandomAccessFile(dest.data(attr), "r");
        data.seek(pos);
        final InputStream istream = Channels.newInputStream(data.getChannel());
        final Numbers restored = new SimpleNumbers();
        restored.load(istream);
        istream.close();
        MatcherAssert.assertThat(
            restored.next(Long.MAX_VALUE),
            Matchers.equalTo(msg)
        );
        MatcherAssert.assertThat(
            IteratorUtils.toList(dest.catalog(attr).iterator()).size(),
            Matchers.equalTo(1)
        );
    }

    /**
     * Save numbers to file.
     * @param file The file to save to
     * @param nums Numbers to save
     * @throws Exception If there is some problem inside
     */
    private static void save(final File file, final long... nums)
        throws Exception {
        final Numbers numbers = new SimpleNumbers();
        for (long num : nums) {
            numbers.add(num);
        }
        final OutputStream output = new FileOutputStream(file);
        numbers.save(output);
        output.close();
    }

}
