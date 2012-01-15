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
package com.netbout.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;

/**
 * UTC time zone.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class Utc {

    /**
     * The calendar to use.
     */
    private static final Calendar CALENDAR =
        Calendar.getInstance(new SimpleTimeZone(0, "UTC"));

    /**
     * It's a utility class.
     */
    private Utc() {
        // empty
    }

    /**
     * Convert date to timestamp.
     * @param date The date
     * @return The timestamp
     * @throws SQLException If some SQL problem inside
     */
    public static void setTimestamp(final PreparedStatement stmt, final int pos,
        final Date date) throws SQLException {
        stmt.setTimestamp(pos, new Timestamp(date.getTime()), Utc.CALENDAR);
    }

    /**
     * Get list of numbers of all bout messages.
     * @param bout The bout where it happened
     * @return List of numbers
     * @throws SQLException If some SQL problem inside
     */
    public static Date getTimestamp(final ResultSet rset, final int pos)
        throws SQLException {
        return new Date(rset.getTimestamp(pos, Utc.CALENDAR).getTime());
    }

}
