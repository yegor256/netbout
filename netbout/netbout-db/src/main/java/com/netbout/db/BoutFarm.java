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
     * @throws SQLException If some SQL problem inside
     */
    @Operation("get-next-bout-number")
    public Long getNextBoutNumber() throws SQLException {
        final Connection conn = Database.connection();
        Long number;
        try {
            final Statement stmt = conn.createStatement();
            stmt.execute("INSERT INTO bout (title) VALUES ('')");
            final ResultSet rset = stmt.getGeneratedKeys();
            rset.next();
            number = rset.getLong(1);
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#getNextBoutNumber(): retrieved %d",
            number
        );
        return number;
    }

    /**
     * New bout was just started.
     * @param number Number of the bout just started
     * @throws SQLException If some SQL problem inside
     */
    @Operation("started-new-bout")
    public void startedNewBout(final Long number) throws SQLException {
        final Connection conn = Database.connection();
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "UPDATE bout SET active = 1 WHERE number = ?"
            );
            stmt.setLong(1, number);
            final int updated = stmt.executeUpdate();
            if (updated != 1) {
                throw new SQLException("Bout not found");
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#startedNewBout(%d): updated",
            number
        );
    }

    /**
     * Get bout title.
     * @param number Number of bout
     * @return The title
     * @throws SQLException If some SQL problem inside
     */
    @Operation("get-bout-title")
    public String getBoutTitle(final Long number) throws SQLException {
        final Connection conn = Database.connection();
        String title;
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "SELECT title FROM bout WHERE number = ?"
            );
            stmt.setLong(1, number);
            final ResultSet rset = stmt.executeQuery();
            rset.next();
            title = rset.getString(1);
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#getBoutTitle(%d): retrieved '%s'",
            number,
            title
        );
        return title;
    }

    /**
     * Bout title was just changed.
     * @param number Number of bout
     * @param title New title
     * @throws SQLException If some SQL problem inside
     */
    @Operation("changed-bout-title")
    public void changedBoutTitle(final Long number, final String title)
        throws SQLException {
        final Connection conn = Database.connection();
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "UPDATE bout SET title = ? WHERE number = ?"
            );
            stmt.setString(1, title);
            stmt.setLong(2, number);
            final int updated = stmt.executeUpdate();
            if (updated != 1) {
                throw new SQLException("Bout not found, title can't be changed");
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#changedBoutTitle(%d, '%s'): updated",
            number,
            title
        );
    }

}
