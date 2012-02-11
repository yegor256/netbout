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

import java.util.Date;

/**
 * Period.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public interface Period {

    /**
     * Maximum number of items to show, no matter what.
     */
    int MAX = 5;

    /**
     * Minimum number of items to show, no matter what.
     */
    int MIN = 3;

    /**
     * This date fits into this period?
     * @param date The date
     * @return Whether it can be accepted in this period ({@code TRUE}) or
     *  this period is full and we should use {@link #next()} in order to get
     *  the next one
     * @throws PeriodViolationException If this date is out of this period
     */
    boolean fits(Date date) throws PeriodViolationException;

    /**
     * Add new date to it.
     * @param date The date
     * @throws PeriodViolationException If this new date is against the rules
     */
    void add(Date date) throws PeriodViolationException;

    /**
     * Get next period.
     * @param date This date should start the new one
     * @return The period, which goes right after this one and is
     *  one size bigger
     * @throws PeriodViolationException If this new date is against the rules
     */
    Period next(Date date) throws PeriodViolationException;

    /**
     * Create query from this period.
     * @param query Original query
     * @return The query
     */
    String query(String query);

    /**
     * Convert it to text.
     * @return Text presentation of this period
     */
    String title();

    /**
     * Explain this period for admin/super-user (mostly used in error
     * messages to explain you what's inside the object).
     * @return The text
     */
    String explain();

}
