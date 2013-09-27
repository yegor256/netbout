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
 * this code accidentally and without intent to use it, please report this
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

import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.NotEmptyHandler;
import com.jcabi.jdbc.SingleHandler;
import com.jcabi.jdbc.Utc;
import com.jcabi.jdbc.VoidHandler;
import com.netbout.spi.cpa.Farm;
import com.netbout.spi.cpa.Operation;
import java.sql.SQLException;
import java.util.Date;

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
     * @throws SQLException If fails
     */
    @Operation("get-next-bout-number")
    public Long getNextBoutNumber() throws SQLException {
        return new JdbcSession(Database.source())
            .sql("INSERT INTO bout (date) VALUES (?)")
            .set(new Utc())
            .insert(new SingleHandler<Long>(Long.class));
    }

    /**
     * Check bout existence.
     * @param number Bout number to check
     * @return It exists?
     * @throws SQLException If fails
     */
    @Operation("check-bout-existence")
    public Boolean checkBoutExistence(final Long number) throws SQLException {
        return new JdbcSession(Database.source())
            // @checkstyle LineLength (1 line)
            .sql("SELECT number FROM bout WHERE number = ? AND title IS NOT NULL")
            .set(number)
            .select(new NotEmptyHandler());
    }

    /**
     * New bout was just started.
     * @param number Number of the bout just started
     * @throws SQLException If fails
     */
    @Operation("started-new-bout")
    public void startedNewBout(final Long number) throws SQLException {
        final Boolean exists = new JdbcSession(Database.source())
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
     * Get number of first bout message.
     * @param number Number of bout
     * @return The message number or NULL if it's absent
     * @throws SQLException If fails
     */
    @Operation("first-bout-message")
    public Long firstBoutMessage(final Long number) throws SQLException {
        return new JdbcSession(Database.source())
            .sql("SELECT MAX(number) FROM message WHERE bout = ?")
            .set(number)
            .select(new SingleHandler<Long>(Long.class, true));
    }

    /**
     * Get bout title.
     * @param number Number of bout
     * @return The title
     * @throws SQLException If fails
     */
    @Operation("get-bout-title")
    public String getBoutTitle(final Long number) throws SQLException {
        return new JdbcSession(Database.source())
            .sql("SELECT title FROM bout WHERE number = ?")
            .set(number)
            .select(new SingleHandler<String>(String.class));
    }

    /**
     * Get bout date.
     * @param number Number of bout
     * @return The date
     * @throws SQLException If fails
     */
    @Operation("get-bout-date")
    public Date getBoutDate(final Long number) throws SQLException {
        return new JdbcSession(Database.source())
            .sql("SELECT date FROM bout WHERE number = ?")
            .set(number)
            .select(new SingleHandler<Utc>(Utc.class))
            .getDate();
    }

    /**
     * Bout title was just changed.
     * @param number Number of bout
     * @param title New title
     * @throws SQLException If fails
     */
    @Operation("changed-bout-title")
    public void changedBoutTitle(final Long number, final String title)
        throws SQLException {
        new JdbcSession(Database.source())
            .sql("UPDATE bout SET title = ? WHERE number = ?")
            .set(title)
            .set(number)
            .update(new VoidHandler());
    }

}
