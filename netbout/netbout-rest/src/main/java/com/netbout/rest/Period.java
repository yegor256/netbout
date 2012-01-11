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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Period.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class Period {

    /**
     * Add dates in it.
     */
    private final transient Set<Date> dates = new TreeSet<Date>();

    /**
     * Public ctor.
     */
    public Period() {
        final Date today = new Date();
        this.dates.add(today);
        this.dates.add(new Date(today.getTime() + 5 * 24 * 60 * 60 * 1000));
    }

    /**
     * Add new date to it.
     * @param date The date
     * @return Whether it is accepted in this period ({@code TRUE}) or
     *  this period is full and we should use {@link #next()} in order to get
     *  the next one
     */
    public boolean add(final Date date) {
        this.dates.add(date);
        return this.dates.size() < 20;
    }

    /**
     * Get next period.
     * @return The period, which goes right after this one and is
     *  one size bigger
     */
    public Period next() {
        final Period next = new Period();
        next.add(new Date(this.finish().getTime() + 1));
        next.add(
            new Date(
                this.finish().getTime()
                + (this.finish().getTime() - this.start().getTime()) * 2
            )
        );
        return next;
    }

    /**
     * Convert it to string.
     * @return Text presentation of this period
     */
    @Override
    public String toString() {
        return String.format(
            "%d-%d",
            this.start().getTime()
            this.finish().getTime()
        );
    }

    /**
     * Build it back from text.
     * @param text The text
     * @return The period discovered
     */
    public static Period valueOf(final String text) {
        final String[] parts = text.split("-");
        final Period period = new Period();
        period.add(new Date(Long.valueOf(parts[0])));
        period.add(new Date(Long.valueOf(parts[1])));
        return period;
    }

    /**
     * Convert it to text.
     * @return Text presentation of this period
     */
    public String title() {
    }

    /**
     * Start of the period.
     * @return Start date
     */
    public Date start() {
    }

    /**
     * Finish of the period.
     * @return Finish date
     */
    public Date finish() {
    }

}
