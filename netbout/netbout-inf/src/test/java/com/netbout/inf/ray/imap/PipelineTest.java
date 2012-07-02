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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case of {@link Pipeline}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class PipelineTest {

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * Pipeline can create an iterator from two sources.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void mergesTwoIteratorsIntoOne() throws Exception {
        final File dir = this.temp.newFolder("foo");
        final Attribute attr = new Attribute("boom-boom-boom");
        final Draft draft = this.draft(new File(dir, "draft"), attr);
        final Baseline src = this.baseline(new File(dir, "src"), attr);
        final Baseline dest = new Baseline(new Lock(new File(dir, "dest")));
        final Pipeline pipe = new Pipeline(draft, dest, src, attr);
        final List<Catalog.Item> items = new LinkedList<Catalog.Item>();
        while (pipe.hasNext()) {
            items.add(pipe.next());
        }
        draft.close();
        src.close();
        MatcherAssert.assertThat(
            dest.data(attr).length(),
            Matchers.greaterThanOrEqualTo(
                (long) Numbers.SIZE * PipelineTest.values().length
            )
        );
        MatcherAssert.assertThat(
            items,
            Matchers.hasSize(PipelineTest.values().length)
        );
        final long pos = items.get(0).position();
        final RandomAccessFile data =
            new RandomAccessFile(dest.data(attr), "r");
        data.seek(pos);
        final InputStream istream = Channels.newInputStream(data.getChannel());
        final Numbers restored = new SimpleNumbers();
        restored.load(istream);
        istream.close();
        MatcherAssert.assertThat(
            restored.next(Long.MAX_VALUE),
            Matchers.not(Matchers.equalTo(0L))
        );
    }

    /**
     * Create draft with given value.
     * @param file The file to save to
     * @param attr Attribute
     * @return The draft created
     * @throws IOException If there is some problem inside
     */
    private Draft draft(final File file,
        final Attribute attr) throws Exception {
        final Draft draft = new Draft(new Lock(file));
        for (String value : PipelineTest.values()) {
            final File temp = draft.numbers(attr);
            draft.backlog(attr).add(
                new Backlog.Item(
                    value,
                    FilenameUtils.getName(temp.getPath())
                )
            );
            final OutputStream output = new FileOutputStream(temp);
            PipelineTest.numbers().save(output);
            output.close();
        }
        return draft;
    }

    /**
     * Create baseline with given value.
     * @param file The file to save to
     * @param attr Attribute
     * @return The baseline created
     * @throws IOException If there is some problem inside
     */
    private Baseline baseline(final File file,
        final Attribute attr) throws Exception {
        final Baseline base = new Baseline(new Lock(file));
        final OutputStream output = new FileOutputStream(base.data(attr));
        long pos = 0;
        final List<Catalog.Item> items = new LinkedList<Catalog.Item>();
        final Numbers numbers = new SimpleNumbers();
        for (String value : PipelineTest.values()) {
            items.add(new Catalog.Item(value, pos));
            pos += numbers.save(output);
        }
        output.close();
        Collections.sort(items);
        base.catalog(attr).create(items.iterator());
        return base;
    }

    /**
     * Get all values.
     * @return Array of values
     */
    private static String[] values() {
        return new String[] {
            "TlYhv", "UMYhv", "TkyJW", "alpha", "beta", "gamma",
        };
    }

    /**
     * Get pre-filled numbers.
     * @return Numbers
     */
    private static Numbers numbers() {
        final Numbers numbers = new SimpleNumbers();
        final int total = new Random().nextInt(10);
        for (int num = 0; num < total; ++num) {
            numbers.add(MsgMocker.number());
        }
        return numbers;
    }

}
