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
package com.netbout.db;

import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link BoutFarm}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class BoutFarmTest {

    /**
     * Farm to work with.
     */
    private final transient BoutFarm farm = new BoutFarm();

    /**
     * Bout number persistence.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testBoutNumbering() throws Exception {
        final Long first = this.farm.getNextBoutNumber();
        MatcherAssert.assertThat(first, Matchers.greaterThan(0L));
        final Long second = this.farm.getNextBoutNumber();
        MatcherAssert.assertThat(second, Matchers.equalTo(first + 1));
    }

    /**
     * Let's record that a new bout was just started.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testRecordBoutStartingEvent() throws Exception {
        final Long num = this.farm.getNextBoutNumber();
        this.farm.startedNewBout(num);
    }

    /**
     * Starting new bout with invalid number should lead to exception.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRecordBoutStartingWithInvalidNumber() throws Exception {
        // @checkstyle MagicNumber (1 line)
        this.farm.startedNewBout(777L);
    }

    /**
     * Let's change bout title and read it back.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testBoutTitleChanging() throws Exception {
        final Long num = this.farm.getNextBoutNumber();
        this.farm.startedNewBout(num);
        MatcherAssert.assertThat(
            this.farm.getBoutTitle(num),
            Matchers.equalTo("")
        );
        final String title = "interesting discussion, \u0443\u0440\u0430!";
        this.farm.changedBoutTitle(num, title);
        MatcherAssert.assertThat(
            this.farm.getBoutTitle(num),
            Matchers.equalTo(title)
        );
    }

    /**
     * BoutFarm can set and read bout date.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void readsBoutCreationDate() throws Exception {
        final Long num = this.farm.getNextBoutNumber();
        this.farm.startedNewBout(num);
        MatcherAssert.assertThat(
            this.farm.getBoutDate(num),
            Matchers.notNullValue()
        );
    }

    /**
     * Check bout existence.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testBoutExistenceChecking() throws Exception {
        final Long num = this.farm.getNextBoutNumber();
        this.farm.startedNewBout(num);
        MatcherAssert.assertThat(
            this.farm.checkBoutExistence(num),
            Matchers.equalTo(true)
        );
        MatcherAssert.assertThat(
            this.farm.checkBoutExistence(this.farm.getNextBoutNumber()),
            Matchers.equalTo(false)
        );
        MatcherAssert.assertThat(
            this.farm.checkBoutExistence(new Random().nextLong()),
            Matchers.equalTo(false)
        );
    }

}
