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
 * this code accidentally and without intent to use it, please report this
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

import com.jcabi.log.VerboseRunnable;
import java.io.File;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case of {@link NfsFolder}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.DoNotUseThreads")
public final class NfsFolderTest {

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * PEM file.
     */
    private final transient URL pem =
        NfsFolderTest.class.getResource("ebs.pem");

    /**
     * NfsFolder can check status through SSH.
     *
     * <p>In order to run this test you should add "ebs.pem" key to your
     * "~/.m2/settings.xml", to the right profile. And then execute Maven:
     * "mvn -Dci -Pnetbout test -Dtest=NfsFolderTest". Should work, if the
     * key you pointed to is correct. Yes, "ebs.pem" property should point
     * to the file you should download first from Amazon IAM (it has to have
     * full access to EBS/EC2).
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    @org.junit.Ignore
    public void checksMountingStatusThroughSsh() throws Exception {
        Assume.assumeThat(this.pem, Matchers.notNullValue());
        final NfsFolder folder = new NfsFolder(this.temp.newFolder("a"));
        MatcherAssert.assertThat(
            folder.path(),
            Matchers.notNullValue()
        );
    }

    /**
     * NfsFolder can throw exception when PEM is absent.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = java.io.IOException.class)
    public void throwsWhenPemIsAbsent() throws Exception {
        Assume.assumeThat(this.pem, Matchers.nullValue());
        new NfsFolder(new File("/mnt/inf")).path();
    }

    /**
     * NfsFolder can yield to another NfsFolder.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void yieldsToAnotherNfsFolder() throws Exception {
        final File dir = this.temp.newFolder("foo");
        final Folder first = new NfsFolder(dir);
        MatcherAssert.assertThat(first.isWritable(), Matchers.is(true));
        final AtomicReference<Folder> second = new AtomicReference<Folder>();
        new Thread(
            new VerboseRunnable(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            second.set(new NfsFolder(dir));
                        } catch (java.io.IOException ex) {
                            throw new IllegalArgumentException(ex);
                        }
                    }
                }
            )
        ).start();
        while (first.isWritable()) {
            // @checkstyle MagicNumber (1 line)
            TimeUnit.MILLISECONDS.sleep(50);
        }
        first.close();
        while (second.get() == null) {
            TimeUnit.MILLISECONDS.sleep(1);
        }
        MatcherAssert.assertThat(second.get().isWritable(), Matchers.is(true));
        second.get().close();
    }

    /**
     * NfsFolder can start after a cancelled folder.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void startsAfterCancelledNfsFolder() throws Exception {
        final File dir = this.temp.newFolder("foo-3");
        final Folder first = new NfsFolder(dir);
        MatcherAssert.assertThat(first.isWritable(), Matchers.is(true));
        final Folder second = new NfsFolder(dir);
        MatcherAssert.assertThat(second.isWritable(), Matchers.is(true));
        first.close();
        second.close();
    }

}
