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

import com.netbout.spi.cpa.Farm;
import com.netbout.spi.cpa.Operation;
import com.ymock.util.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Bout manipulations.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Farm
public final class BoutFarm {

    /**
     * Read next bout number.
     * @return Next bout number
     */
    @Operation("get-next-bout-number")
    public Long getNextBoutNumber() {
        return new DbSession()
            .sql("INSERT INTO bout (date) VALUES (?)")
            .set(new Date())
            .insert(
                new Handler<Long>() {
                    @Override
                    public Long handle(final ResultSet rset)
                        throws SQLException {
                        rset.next();
                        return rset.getLong(1);
                    }
                }
            );
    }

    /**
     * Check bout existence.
     * @param number Bout number to check
     * @return It exists?
     */
    @Operation("check-bout-existence")
    public Boolean checkBoutExistence(final Long number) {
        return new DbSession()
            .sql("SELECT number FROM bout WHERE number = ? AND title IS NOT NULL")
            .set(number)
            .select(new NotEmptyHandler());
    }

    /**
     * New bout was just started.
     * @param number Number of the bout just started
     */
    @Operation("started-new-bout")
    public void startedNewBout(final Long number) {
        final Boolean exists = new DbSession()
            .sql("SELECT number FROM bout WHERE number = ?")
            .set(number)
            .select(new NotEmptyHandler());
        if (!exists) {
            throw new IllegalArgumentException(
                String.format("Bout #%d not found, can't record", number)
            );
        }
        this.changedBoutTitle(number, "");
    }

    /**
     * Get list of messages that belong to the specified bout.
     * @param bout Number of the bout to work with
     * @return List of message numbers
     */
    @Operation("get-bout-messages")
    public List<Long> getBoutMessages(final Long bout) {
        return new DbSession()
            // @checkstyle LineLength (1 line)
            .sql("SELECT number FROM message WHERE bout = ? AND date IS NOT NULL ORDER BY date")
            .set(bout)
            .select(
                new Handler<List<Long>>() {
                    @Override
                    public List<Long> handle(final ResultSet rset)
                        throws SQLException {
                        final List<Long> numbers = new ArrayList<Long>();
                        while (rset.next()) {
                            numbers.add(rset.getLong(1));
                        }
                        return numbers;
                    }
                }
            );
    }

    /**
     * Get bout title.
     * @param number Number of bout
     * @return The title
     */
    @Operation("get-bout-title")
    public String getBoutTitle(final Long number) {
        return new DbSession()
            .sql("SELECT title FROM bout WHERE number = ?")
            .set(number)
            .select(
                new Handler<String>() {
                    @Override
                    public String handle(final ResultSet rset)
                        throws SQLException {
                        if (!rset.next()) {
                            throw new IllegalArgumentException(
                                String.format(
                                    "Bout #%d not found, can't read title",
                                    number
                                )
                            );
                        }
                        return rset.getString(1);
                    }
                }
            );
    }

    /**
     * Get bout date.
     * @param number Number of bout
     * @return The date
     */
    @Operation("get-bout-date")
    public Date getBoutDate(final Long number) {
        return new DbSession()
            .sql("SELECT date FROM bout WHERE number = ?")
            .set(number)
            .select(
                new Handler<Date>() {
                    @Override
                    public Date handle(final ResultSet rset)
                        throws SQLException {
                        if (!rset.next()) {
                            throw new IllegalArgumentException(
                                String.format(
                                    "Bout #%d not found, can't read its date",
                                    number
                                )
                            );
                        }
                        return Utc.getTimestamp(rset, 1);
                    }
                }
            );
    }

    /**
     * Bout title was just changed.
     * @param number Number of bout
     * @param title New title
     */
    @Operation("changed-bout-title")
    public void changedBoutTitle(final Long number, final String title) {
        new DbSession()
            .sql("UPDATE bout SET title = ? WHERE number = ?")
            .set(title)
            .set(number)
            .update();
    }

}
