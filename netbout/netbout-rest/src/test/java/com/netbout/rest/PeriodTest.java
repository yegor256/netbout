/**
 * Copyright (c) 2009-2011, netBout.com
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
package com.netbout.rest;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Period}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class PeriodTest {

    /**
     * Period can accept dates and return title.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void concumesDatesAndReturnsTitle() throws Exception {
        final Period period = new Period().next(this.date("2008-08-24"));
        period.add(this.date("2008-08-22"));
        // MatcherAssert.assertThat(
        //     period.title(),
        //     Matchers.equalTo("August 2008")
        // );
    }

    /**
     * Period can create its next one.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void nextPeriodIsEarlierThanCurrentOne() throws Exception {
        final Period period = new Period();
        final Period next = period.next(this.date("2008-05-13"));
        MatcherAssert.assertThat(
            "is older than finish",
            next.newest().before(period.newest())
        );
    }

    /**
     * Period can be serialized to string and back.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void serializesToStringAndBack() throws Exception {
        final Period period = new Period();
        final String text = period.toString();
        MatcherAssert.assertThat(
            Period.valueOf(text),
            Matchers.equalTo(period)
        );
    }

    /**
     * Period can add a date which is the same as newest.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void addsNewestTwice() throws Exception {
        final Period period = new Period();
        MatcherAssert.assertThat(
            "newest fits in",
            period.fits(period.newest())
        );
    }

    /**
     * Period can fit new dates correctly.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void fitsNewDatesCorrectly() throws Exception {
        final Period period = new Period().next(this.date("2011-03-20"));
        MatcherAssert.assertThat(
            "new date fits in",
            period.fits(this.date("2011-03-18"))
        );
    }

    /**
     * Period can understand NULL gracefully.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void revertsFromNull() throws Exception {
        final Period period = Period.valueOf(null);
    }

    /**
     * String to date.
     * @param text The text
     * @return The date
     * @throws java.text.ParseException If failed to parse
     */
    private Date date(final String text) throws java.text.ParseException {
        return new SimpleDateFormat("yyyy-MM-dd").parse(text);
    }

}
