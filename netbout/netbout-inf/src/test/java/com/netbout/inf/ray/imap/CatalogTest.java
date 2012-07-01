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

import com.jcabi.log.VerboseThreads;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case of {@link Catalog}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle MagicNumber (500 lines)
 */
public final class CatalogTest {

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * Catalog can register value and find it laters.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void registersValueAndFindsItThen() throws Exception {
        final Catalog catalog = new Catalog(this.temp.newFile("catalog.txt"));
        final String value = "some value to use, \u0433";
        final long pos = Math.max(Math.abs(new Random().nextLong()), 1L);
        final int total = new Random().nextInt(500) + 100;
        final int length = 5;
        final List<Catalog.Item> items = new ArrayList<Catalog.Item>(total + 1);
        items.add(new Catalog.Item(value, pos));
        for (int num = 0; num < total; ++num) {
            items.add(new Catalog.Item(CatalogTest.random(), num));
        }
        Collections.sort(items);
        catalog.create(items.iterator());
        MatcherAssert.assertThat(catalog.seek(value), Matchers.equalTo(pos));
        MatcherAssert.assertThat(catalog.seek("absent"), Matchers.lessThan(0L));
    }

    /**
     * Catalog can register value and find it in the iterator.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void registersValueAndFindsItInIterator() throws Exception {
        final Catalog catalog = new Catalog(this.temp.newFile("catalog-2.txt"));
        final String value = "some value to use, \u0433";
        final long pos = Math.max(Math.abs(new Random().nextLong()), 1L);
        final int total = new Random().nextInt(500) + 100;
        final int length = 5;
        final List<Catalog.Item> items = new ArrayList<Catalog.Item>();
        final Catalog.Item item = new Catalog.Item(value, pos);
        items.add(item);
        for (int num = 0; num < total; ++num) {
            items.add(new Catalog.Item(CatalogTest.random(), num));
        }
        Collections.sort(items);
        catalog.create(items.iterator());
        MatcherAssert.assertThat(
            IteratorUtils.toArray(catalog.iterator()),
            Matchers.<Object>hasItemInArray(item)
        );
    }

    /**
     * Catalog can work correctly with duplicates.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void handlesDuplicatesCorrectly() throws Exception {
        final Catalog catalog = new Catalog(this.temp.newFile("catalog-3.txt"));
        final String first = "TlYhv";
        final String second = "UMYhv";
        MatcherAssert.assertThat(
            first.hashCode(),
            Matchers.equalTo(second.hashCode())
        );
        final List<Catalog.Item> items = new ArrayList<Catalog.Item>(2);
        items.add(new Catalog.Item(first, 1));
        items.add(new Catalog.Item(second, 2));
        Collections.sort(items);
        catalog.create(items.iterator());
        MatcherAssert.assertThat(catalog.seek(first), Matchers.equalTo(1L));
        MatcherAssert.assertThat(catalog.seek(second), Matchers.equalTo(2L));
    }

    /**
     * Catalog can work in many threads in parallel.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void supportsMultiThreadingSearch() throws Exception {
        final Catalog catalog = new Catalog(this.temp.newFile("catalog-4.txt"));
        final int total = new Random().nextInt(100) + 50;
        final int length = 5;
        final List<Catalog.Item> items = new ArrayList<Catalog.Item>();
        for (int num = 0; num < total; ++num) {
            items.add(new Catalog.Item(CatalogTest.random(), num));
        }
        Collections.sort(items);
        catalog.create(items.iterator());
        final int threads = 10;
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch latch = new CountDownLatch(threads);
        final Callable<?> task = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                start.await();
                for (int attempt = 0; attempt < 20; ++attempt) {
                    catalog.seek(CatalogTest.random());
                }
                latch.countDown();
                return null;
            }
        };
        final ExecutorService svc =
            Executors.newFixedThreadPool(threads, new VerboseThreads());
        for (int thread = 0; thread < threads; ++thread) {
            svc.submit(task);
        }
        start.countDown();
        latch.await(1, TimeUnit.SECONDS);
        svc.shutdown();
    }

    /**
     * Generate random string.
     * @return The string
     */
    private static String random() {
        return RandomStringUtils.random(new Random().nextInt(6) + 1);
    }

}
