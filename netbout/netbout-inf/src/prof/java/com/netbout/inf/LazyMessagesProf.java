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
package com.netbout.inf;

import com.jcabi.log.Logger;
import com.netbout.inf.functors.DefaultStore;
import com.netbout.inf.ray.MemRay;
import com.netbout.inf.ray.SnapshotMocker;
import java.io.File;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.mockito.Mockito;

/**
 * Profiler of {@link LazyMessages}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class LazyMessagesProf {

    /**
     * Main entrance, for profiler.
     * @param args Optional arguments
     * @throws Exception If there is some problem inside
     */
    public static void main(final String... args) throws Exception {
        new LazyMessagesProf().run();
    }

    /**
     * Run it (use "urn:facebook:1531296526" for the big-data test).
     * @throws Exception If there is some problem inside
     */
    private void run() throws Exception {
        final File dir = new FolderMocker().mock().path();
        final int total = 150000;
        new SnapshotMocker(dir)
            .withMaximum(total)
            .withBouts(50, 5000)
            .withAttr("talks-with", "urn:test:", 5)
            .withAttr("bundled-marker", "marker-", 100)
            .mock();
        final Ray ray = new MemRay(dir);
        final List<Long> first = this.fetch(ray, this.term(ray));
        MatcherAssert.assertThat(
            first,
            Matchers.hasSize(Matchers.greaterThan(0))
        );
        final List<Long> second = this.fetch(ray, this.term(ray));
        MatcherAssert.assertThat(
            second,
            Matchers.equalTo(first)
        );
    }

    /**
     * Create term from ray.
     * @param ray The ray to use
     * @throws Exception If there is some problem inside
     */
    private Term term(final Ray ray) throws Exception {
        return new ParserAdapter(new DefaultStore())
            // @checkstyle LineLength (1 line)
            .parse("(and (talks-with 'urn:test:1') (from 0) (bundled) (unique $bout.number))")
            .term(ray);
    }

    /**
     * Convert ray and term to list of message numbers.
     * @param ray The ray
     * @param term The term
     * @return Message numbers (as a list)
     */
    private List<Long> fetch(final Ray ray, final Term term) {
        final List<Long> msgs = new LinkedList<Long>();
        final long start = System.currentTimeMillis();
        final Iterator<Long> iterator = new LazyMessages(ray, term).iterator();
        while (iterator.hasNext()) {
            msgs.add(iterator.next());
        }
        Logger.debug(
            this,
            "#fetch(): %[list]s in %[ms]s",
            msgs,
            System.currentTimeMillis() - start
        );
        return msgs;
    }

}
