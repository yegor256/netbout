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

import com.ymock.util.Logger;
import java.util.Date;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.commons.lang.ArrayUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Period.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class Period {

    /**
     * Maximum number of items to show, no matter what.
     */
    public static final int MAX = 5;

    /**
     * Minimum number of items to show, no matter what.
     */
    public static final int MIN = 3;

    /**
     * Default limit, in milliseconds.
     */
    public static final long DEFAULT_LIMIT = 12L * 30 * 24 * 60 * 60 * 1000;

    /**
     * Duration in milliseconds, which we never break.
     */
    public static final long UNBREAKEN = 1000 * 60L;

    /**
     * Add dates in it.
     */
    private final transient SortedSet<Date> dates = new TreeSet<Date>();

    /**
     * Start of the period, the newest date, or NULL if it's NOW.
     */
    private final transient Date start;

    /**
     * Maximum distance between dates, in milliseconds.
     */
    private final transient Long limit;

    /**
     * Public ctor.
     */
    public Period() {
        this(null, Period.DEFAULT_LIMIT);
    }

    /**
     * Public ctor.
     * @param from When to start
     * @param lmt The limit
     */
    private Period(final Date from, final long lmt) {
        this.start = from;
        this.limit = lmt;
    }

    /**
     * Build it back from text.
     * @param text The text
     * @return The period discovered
     */
    public static Period valueOf(final String text) {
        Period period;
        if (text != null && text.matches("^\\d+t\\d+$")) {
            final String[] parts = text.split("t");
            period = new Period(
                new Date(Long.valueOf(parts[0])),
                Long.valueOf(parts[1])
            );
        } else {
            period = new Period();
        }
        return period;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "%dt%d",
            this.newest().getTime(),
            this.limit
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return (obj instanceof Period)
            && ((Period) obj).newest().equals(this.newest())
            && ((Period) obj).limit.equals(this.limit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.newest().hashCode() + this.limit.hashCode();
    }

    /**
     * This date fits into this period?
     * @param date The date
     * @return Whether it can be accepted in this period ({@code TRUE}) or
     *  this period is full and we should use {@link #next()} in order to get
     *  the next one
     */
    public boolean fits(final Date date) {
        final boolean offlimit = date.before(
            new Date(this.newest().getTime() - this.limit)
        );
        final boolean overflow = this.dates.size() >= this.MAX
            && (this.dates.last().getTime() - this.dates.first().getTime())
                > this.UNBREAKEN;
        final boolean fits = !overflow
            && !date.after(this.newest())
            && (this.dates.size() < this.MIN || !offlimit);
        Logger.debug(
            this,
            "#fits(%s): offlimit=%B, overflow=%B, size=%d -> %B",
            date,
            offlimit,
            overflow,
            this.dates.size(),
            fits
        );
        return fits;
    }

    /**
     * Add new date to it.
     * @param date The date
     */
    public void add(final Date date) {
        if (!this.fits(date)) {
            throw new IllegalArgumentException(
                String.format(
                    "Can't add '%s', call #fits() first",
                    date
                )
            );
        }
        this.dates.add(date);
    }

    /**
     * Get next period.
     * @param date This date should start the new one
     * @return The period, which goes right after this one and is
     *  one size bigger
     */
    public Period next(final Date date) {
        if (date.after(this.newest())) {
            throw new IllegalArgumentException(
                String.format(
                    "NEXT #%d '%s' should be older than START '%s'",
                    this.dates.size(),
                    date,
                    this.newest()
                )
            );
        }
        if (!this.dates.isEmpty() && date.after(this.dates.first())) {
            throw new IllegalArgumentException(
                String.format(
                    "NEXT '%s' should be older than '%s' (among %d dates)",
                    date,
                    this.dates.first(),
                    this.dates.size()
                )
            );
        }
        return new Period(date, this.limit * 2);
    }

    /**
     * Newest date.
     * @return The date
     */
    public Date newest() {
        Date newest;
        if (this.start == null) {
            newest = new Date();
        } else {
            newest = this.start;
        }
        return newest;
    }

    /**
     * Create query from this period.
     * @param query Original query
     * @return The query
     */
    public String query(final String query) {
        String original = "";
        if (!query.isEmpty() && query.charAt(0) == '(') {
            original = query;
        } else {
            if (!query.isEmpty()) {
                original = String.format(
                    " (matches '%s' $text)",
                    query.replace("'", "\\'")
                );
            }
        }
        final String text = String.format(
            "(and (not (greater-than $date '%s'))%s)",
            ISODateTimeFormat.dateTime().print(
                new DateTime(this.newest().getTime())
            ),
            original
        );
        Logger.debug(
            this,
            "#format(%s, %s): '%s'",
            query,
            this,
            text
        );
        return text;
    }

    /**
     * Convert it to text.
     * @return Text presentation of this period
     */
    public String title() {
        final org.joda.time.Period distance = new Interval(
            this.newest().getTime(),
            new Date().getTime()
        ).toPeriod();
        String title;
        if (distance.getYears() > 0) {
            title = this.plural("year", distance.getYears());
        } else if (distance.getMonths() > 0) {
            title = this.plural("month", distance.getMonths());
        } else if (distance.getWeeks() > 0) {
            title = this.plural("week", distance.getWeeks());
        } else if (distance.getDays() > 0) {
            title = this.plural("day", distance.getDays());
        } else if (distance.getHours() > 0) {
            title = this.plural("hour", distance.getHours());
        } else if (distance.getMinutes() > 0) {
            title = this.plural("minute", distance.getMinutes());
        } else {
            title = "a few seconds";
        }
        return String.format("%s ago", title);
    }

    /**
     * Create a plural form of the noun.
     * @param noun The noun
     * @param num How many of them
     * @return The text
     */
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    private String plural(final String noun, final int num) {
        final Map<String, String> digits = ArrayUtils.toMap(
            new String[][] {
                {"1", "a"},
                {"2", "two"},
                {"3", "three"},
                {"4", "four"},
                {"5", "five"},
                {"6", "six"},
                {"7", "seven"},
                {"8", "eight"},
                {"9", "nine"},
            }
        );
        String count = Integer.toString(num);
        if (digits.containsKey(count)) {
            count = digits.get(count);
        }
        return String.format(
            "%s %s%s",
            count,
            noun,
            // @checkstyle AvoidInlineConditionals (1 line)
            num == 1 ? "" : "s"
        );
    }

}
