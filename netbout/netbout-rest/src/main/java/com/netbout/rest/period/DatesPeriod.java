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
package com.netbout.rest.period;

import com.netbout.inf.PredicateBuilder;
import com.ymock.util.Logger;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Period.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
final class DatesPeriod implements Period {

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
    public DatesPeriod() {
        this(null, DatesPeriod.DEFAULT_LIMIT);
    }

    /**
     * Public ctor.
     * @param from When to start
     * @param lmt The limit
     */
    public DatesPeriod(final Date from, final long lmt) {
        this.start = from;
        this.limit = lmt;
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
        return (obj instanceof DatesPeriod)
            && ((DatesPeriod) obj).newest().equals(this.newest())
            && ((DatesPeriod) obj).limit.equals(this.limit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.newest().hashCode() + this.limit.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean fits(final Date date) throws PeriodViolationException {
        if (date.after(this.newest())) {
            throw new PeriodViolationException(
                Logger.format(
                    // @checkstyle LineLength (1 line)
                    "New date '%s' can't be newer than START '%s' in '%s': %[list]s",
                    date,
                    this.newest(),
                    this.explain(),
                    this.dates
                )
            );
        }
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
     * {@inheritDoc}
     */
    @Override
    public void add(final Date date) throws PeriodViolationException {
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
     * {@inheritDoc}
     */
    @Override
    public Period next(final Date date) throws PeriodViolationException {
        if (date.after(this.newest())) {
            throw new PeriodViolationException(
                String.format(
                    "NEXT #%d '%s' should be older than START '%s' in '%s'",
                    this.dates.size(),
                    date,
                    this.newest(),
                    this.explain()
                )
            );
        }
        if (!this.dates.isEmpty() && date.after(this.dates.first())) {
            throw new PeriodViolationException(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "NEXT '%s' should be older than '%s' (among %d dates) in '%s'",
                    date,
                    this.dates.first(),
                    this.dates.size(),
                    this.explain()
                )
            );
        }
        return new DatesPeriod(date, this.limit * 2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String query(final String query) {
        String original = "";
        if (!query.isEmpty() && query.charAt(0) == '(') {
            original = query;
        } else {
            if (!query.isEmpty()) {
                original = PredicateBuilder.normalize(query);
            }
        }
        final String text = String.format(
            "(and %s (not (greater-than $date '%s')))",
            original,
            ISODateTimeFormat.dateTime().print(
                new DateTime(this.newest().getTime())
            )
        );
        Logger.debug(
            this,
            "#query(%s): '%s'",
            query,
            text
        );
        return text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String title() {
        return PeriodsBuilder.when(this.newest());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String explain() {
        return Logger.format(
            "%s as (start=%s, limit=%d, %d dates: %[list]s)",
            this,
            this.start,
            this.limit,
            this.dates.size(),
            this.dates
        );
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

}
