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
package com.netbout.inf.index;

import java.net.URL;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
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
     * PEM file.
     */
    private final transient URL pem =
        EbsDirectoryTest.class.getResource("ebs.pem");

    /**
     * EbsDirectory can check status through SSH.
     *
     * <p>In order to run this test you should add "ebs.pem" key to your
     * "~/.m2/settings.xml", to the right profile. And then execute Maven:
     * "mvn -Dci -Pnetbout test -Dtest=EbsDirectoryTest". Should work, if the
     * key you pointed to is correct. Yes, "ebs.pem" property should point
     * to the file you should download first from Amazon IAM (it has to have
     * full access to EBS/EC2).
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    public void checksMountingStatusThroughSsh() throws Exception {
        Assume.assumeThat(this.pem, Matchers.notNullValue());
        final EbsDirectory dir = new EbsDirectory(
            this.temp.newFolder("a"),
            "ec2-23-20-63-25.compute-1.amazonaws.com"
        );
        MatcherAssert.assertThat(
            dir.mounted(),
            Matchers.equalTo(false)
        );
    }

    /**
     * EbsDirectory can throw exception when PEM is absent.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = java.io.IOException.class)
    public void throwsWhenPemIsAbsent() throws Exception {
        Assume.assumeThat(this.pem, Matchers.nullValue());
        new EbsDirectory(this.temp.newFolder("xx")).mounted();
    }

}
