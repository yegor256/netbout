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

import com.netbout.rest.jaxb.Link;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.UriBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Groups dates together.
 *
 * <p>The class is NOT thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class PeriodsBuilder {

    /**
     * How many links to show.
     */
    public static final int MAX_LINKS = 2;

    /**
     * REL for "more" link.
     */
    public static final String REL_MORE = "more";

    /**
     * REL for "earliest" link.
     */
    public static final String REL_EARLIEST = "earliest";

    /**
     * List of links to build.
     */
    private final transient List<Link> periods = new ArrayList<Link>();

    /**
     * Initial period to start with.
     */
    private transient Period period;

    /**
     * Query param.
     */
    private transient String param;

    /**
     * Base URI.
     */
    private final transient UriBuilder base;

    /**
     * Position of the recently added date.
     */
    private transient int position;

    /**
     * Number of a current slide.
     */
    private transient int slide;

    /**
     * How many dates have been accumulated in the current slide so far.
     */
    private transient int total;

    /**
     * Public ctor.
     * @param prd The period to start with
     * @param builder Builder of base URI
     */
    public PeriodsBuilder(final Period prd, final UriBuilder builder) {
        this.period = prd;
        this.base = builder;
    }

    /**
     * Set query param.
     * @param prm The param
     * @return This object
     */
    public PeriodsBuilder setQueryParam(final String prm) {
        this.param = prm;
        return this;
    }

    /**
     * Shall we show this date?
     * @param date The date to show
     * @return Shall we?
     */
    public boolean show(final Date date) {
        if (this.slide >= this.MAX_LINKS) {
            throw new IllegalArgumentException("don't forget to call #more()");
        }
        this.position += 1;
        this.total += 1;
        boolean show = false;
        if (this.period.fits(date)) {
            if (this.slide == 0) {
                show = true;
            }
        } else {
            if (this.slide > 0) {
                this.total -= 1;
                this.periods.add(this.link(this.REL_MORE));
            }
            this.period = this.period.next(date);
            this.slide += 1;
            this.total = 1;
        }
        this.period.add(date);
        Logger.debug(
            this,
            "#show('%s'): pos=%d, slide=%d: %B",
            date,
            this.position,
            this.slide,
            show
        );
        return show;
    }

    /**
     * Shall we continue or it's time to stop?
     * @param size How many there are in total?
     * @return Do we need more or that's enough?
     */
    public boolean more(final int size) {
        boolean more = true;
        if (this.slide >= this.MAX_LINKS) {
            this.total = size - this.position + 1;
            this.periods.add(this.link(this.REL_EARLIEST));
            more = false;
        }
        Logger.debug(
            this,
            "#more(%d): pos=%d, slide=%d: %B",
            size,
            this.position,
            this.slide,
            more
        );
        return more;
    }

    /**
     * Get all created links.
     * @return Links
     */
    public List<Link> links() {
        if (this.slide > 0 && this.slide < this.MAX_LINKS) {
            this.periods.add(this.link(this.REL_MORE));
        }
        return this.periods;
    }

    /**
     * Create query with period.
     * @param query Original query
     * @param period The period
     * @return The query
     */
    public static String format(final String query, final Period period) {
        String original = "";
        if (!query.isEmpty() && query.charAt(0) == '(') {
            original = query;
        } else {
            if (!query.isEmpty()) {
                original = String.format(
                    "(matches '%s' $text)",
                    query.replace("'", "\\'")
                );
            }
        }
        final String text = String.format(
            "(and (not (greater-than $date '%s')) %s)",
            ISODateTimeFormat.dateTime().print(
                new DateTime(period.newest().getTime())
            ),
            original
        );
        Logger.debug(
            PeriodsBuilder.class,
            "#format(%s, %s): '%s'",
            query,
            period,
            text
        );
        return text;
    }

    /**
     * Build link to period.
     * @param name Name of this link
     * @return The link
     */
    private Link link(final String name) {
        return new Link(
            name,
            String.format(
                "%s (%d)",
                this.period.title(),
                this.total
            ),
            this.base.clone().queryParam(this.param, this.period)
        );
    }

}
