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
 * Profiler of {@link DefaultInfinity}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class DefaultInfinityProf {

    /**
     * The randomizer.
     */
    private final transient Random random = new SecureRandom();

    /**
     * Main entrance, for profiler.
     * @param args Optional arguments
     * @throws Exception If there is some problem inside
     */
    public static void main(final String... args) throws Exception {
        new DefaultInfinityProf().run();
    }

    /**
     * Run it (use "urn:facebook:1531296526" for the big-data test).
     * @throws Exception If there is some problem inside
     */
    private void run() throws Exception {
        final Infinity inf = this.prepare();
        final String[] queries = new String[] {
            "(and (or (talks-with 'urn:test:Jeff') (talks-with 'urn:facebook:1531296526')) (bundled) (limit 10))",
            "(and (talks-with 'urn:facebook:1531296526') (unbundled 5615) (unique $bout.number))",
        };
        for (int retry = 0; retry < 2; ++retry) {
            for (String query : queries) {
                MatcherAssert.assertThat(
                    this.fetch(inf.messages(query).iterator()),
                    Matchers.hasSize(Matchers.greaterThan(0))
                );
            }
        }
        inf.close();
    }

    /**
     * Prepare infinity.
     * @return Infinity
     * @throws Exception If there is some problem inside
     */
    private Infinity prepare() throws Exception {
        final Folder folder = new FolderMocker().mock();
        FileUtils.copyDirectory(
            new File("./src/prof/resources/com/netbout/inf/ray-2"),
            new File(folder.path(), "/ray-2")
        );
        return new DefaultInfinity(folder);
    }

    /**
     * Convert iterator to list of message numbers.
     * @param iterator The iterator
     * @return Message numbers (as a list)
     */
    private List<Long> fetch(final Iterator<Long> iterator) {
        final List<Long> msgs = new LinkedList<Long>();
        final long start = System.currentTimeMillis();
        while (iterator.hasNext()) {
            msgs.add(iterator.next());
        }
        Logger.debug(
            this,
            "#fetch(): %d msgs in %[ms]s",
            msgs.size(),
            System.currentTimeMillis() - start
        );
        return msgs;
    }

}
