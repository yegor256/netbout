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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case of {@link Utc}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class UtcTest {

    /**
     * Format to use in tests.
     */
    private transient DateFormat fmt;

    /**
     * Prepare this test case.
     * @throws Exception If there is some problem inside
     */
    @Before
    public void prepare() throws Exception {
        this.fmt = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH
        );
    }

    /**
     * Utc can save date to prepared statement.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void savesDateWithUtcTimezone() throws Exception {
        final Long message =
            new MessageRowMocker(new BoutRowMocker().mock()).mock();
        this.fmt.setCalendar(
            new GregorianCalendar(TimeZone.getTimeZone("GMT-5"))
        );
        final Date date = this.fmt.parse("2008-05-24 05:06:07.000");
        final Connection conn = Database.connection();
        String saved;
        try {
            final PreparedStatement ustmt = conn.prepareStatement(
                "UPDATE message SET date = ? WHERE number = ? "
            );
            Utc.setTimestamp(ustmt, 1, date);
            ustmt.setLong(2, message);
            ustmt.executeUpdate();
            final PreparedStatement rstmt = conn.prepareStatement(
                "SELECT date FROM message WHERE number = ? "
            );
            rstmt.setLong(1, message);
            final ResultSet rset = rstmt.executeQuery();
            try {
                if (!rset.next()) {
                    throw new IllegalArgumentException();
                }
                saved = rset.getString(1);
            } finally {
                rset.close();
            }
        } finally {
            conn.close();
        }
        MatcherAssert.assertThat(
            saved,
            Matchers.startsWith("2008-05-24 10:06:07")
        );
    }

    /**
     * Utc can load date from result set.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void loadsDateWithUtcTimezone() throws Exception {
        final Long message =
            new MessageRowMocker(new BoutRowMocker().mock()).mock();
        final Connection conn = Database.connection();
        Date loaded;
        try {
            final PreparedStatement ustmt = conn.prepareStatement(
                "UPDATE message SET date = ? WHERE number = ?"
            );
            ustmt.setString(1, "2005-02-02 10:07:08.000");
            ustmt.setLong(2, message);
            ustmt.executeUpdate();
            final PreparedStatement rstmt = conn.prepareStatement(
                "SELECT  date FROM message WHERE number = ?"
            );
            rstmt.setLong(1, message);
            final ResultSet rset = rstmt.executeQuery();
            try {
                if (!rset.next()) {
                    throw new IllegalArgumentException();
                }
                loaded = Utc.getTimestamp(rset, 1);
            } finally {
                rset.close();
            }
        } finally {
            conn.close();
        }
        this.fmt.setCalendar(
            new GregorianCalendar(TimeZone.getTimeZone("GMT-3"))
        );
        MatcherAssert.assertThat(
            this.fmt.format(loaded),
            Matchers.startsWith("2005-02-02 07:07:08")
        );
    }

    /**
     * Utc can set and read message date, with different timezone.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void setsAndReadsDateWithDifferentTimezone() throws Exception {
        final Long message =
            new MessageRowMocker(new BoutRowMocker().mock()).mock();
        final Date date = new Date();
        new MessageFarm().changedMessageDate(message, date);
        final Connection conn = Database.connection();
        String saved;
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "SELECT date FROM message WHERE number = ?"
            );
            stmt.setLong(1, message);
            final ResultSet rset = stmt.executeQuery();
            try {
                if (!rset.next()) {
                    throw new IllegalStateException();
                }
                saved = rset.getString(1);
            } finally {
                rset.close();
            }
        } finally {
            conn.close();
        }
        this.fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        final Date absolute = this.fmt.parse(saved);
        MatcherAssert.assertThat(
            absolute.toString(),
            Matchers.equalTo(date.toString())
        );
    }

}
