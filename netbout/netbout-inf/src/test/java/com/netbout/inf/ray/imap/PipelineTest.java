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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
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
@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
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
     * @checkstyle ExecutableStatementCount (100 lines)
     */
    @Test
    public void mergesTwoIteratorsIntoOne() throws Exception {
        final File dir = this.temp.newFolder("foo");
        final Attribute attr = new Attribute("boom-boom-boom");
        final Collection<String> values = ReverseMocker.values(5);
        final Draft draft = this.draft(
            new File(dir, "draft"),
            attr,
            values,
            5
        );
        final Baseline src = this.baseline(
            new File(dir, "src"),
            attr,
            values,
            0
        );
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
                (long) Numbers.SIZE * values.size()
            )
        );
        MatcherAssert.assertThat(
            items,
            Matchers.hasSize(values.size())
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
     * Pipeline can properly order items.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void properlyOrdersItemsInIterators() throws Exception {
        final File dir = this.temp.newFolder("foo-2");
        final Attribute attr = new Attribute("bar-bar");
        final Draft draft = this.draft(
            new File(dir, "draft-2"),
            attr,
            ReverseMocker.values(10),
            10
        );
        final Baseline src = this.baseline(
            new File(dir, "src-2"),
            attr,
            ReverseMocker.values(10),
            10
        );
        final Baseline dest = new Baseline(new Lock(new File(dir, "dest-2")));
        final Pipeline pipe = new Pipeline(draft, dest, src, attr);
        int previous = Integer.MIN_VALUE;
        while (pipe.hasNext()) {
            final Catalog.Item item = pipe.next();
            final int hash = item.hashCode();
            MatcherAssert.assertThat(
                hash,
                Matchers.greaterThanOrEqualTo(previous)
            );
            previous = hash;
        }
        draft.close();
        src.close();
    }

    /**
     * Create draft with given value.
     * @param file The file to save to
     * @param attr Attribute
     * @param vals Values to use
     * @return The draft created
     * @throws Exception If there is some problem inside
     */
    private Draft draft(final File file, final Attribute attr,
        final Collection<String> vals, final int max) throws Exception {
        final Draft draft = new Draft(new Lock(file));
        for (String value : vals) {
            final File tmp = draft.numbers(attr);
            draft.backlog(attr).add(
                new Backlog.Item(
                    value,
                    FilenameUtils.getName(tmp.getPath())
                )
            );
            final OutputStream output = new FileOutputStream(tmp);
            new NumbersMocker().withMaximum(max).mock().save(output);
            output.close();
        }
        return draft;
    }

    /**
     * Create baseline with given value.
     * @param file The file to save to
     * @param attr Attribute
     * @param vals Values to use
     * @param max Maximum number of numbers to put in every one
     * @return The baseline created
     * @throws Exception If there is some problem inside
     */
    private Baseline baseline(final File file,
        final Attribute attr, final Collection<String> vals,
        final int max) throws Exception {
        final Baseline base = new Baseline(new Lock(file));
        final OutputStream output = new FileOutputStream(base.data(attr));
        long pos = 0;
        final List<Catalog.Item> items = new LinkedList<Catalog.Item>();
        for (String value : vals) {
            items.add(new Catalog.Item(value, pos));
            pos += new NumbersMocker().withMaximum(max).mock().save(output);
        }
        output.close();
        Collections.sort(items);
        base.catalog(attr).create(items.iterator());
        return base;
    }

}
