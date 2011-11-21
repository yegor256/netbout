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
     * @throws SQLException If some SQL problem inside
     */
    @Operation("get-next-bout-number")
    public Long getNextBoutNumber() throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        Long number;
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO bout () VALUES ()",
                Statement.RETURN_GENERATED_KEYS
            );
            stmt.execute();
            final ResultSet rset = stmt.getGeneratedKeys();
            try {
                if (!rset.next()) {
                    throw new IllegalStateException(
                        "No bouts were inserted"
                    );
                }
                number = rset.getLong(1);
            } finally {
                rset.close();
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#getNextBoutNumber(): retrieved %d [%dms]",
            number,
            System.currentTimeMillis() - start
        );
        return number;
    }

    /**
     * Check bout existence.
     * @param number Bout number to check
     * @return It exists?
     * @throws SQLException If some SQL problem inside
     */
    @Operation("check-bout-existence")
    public Boolean checkBoutExistence(final Long number) throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        Boolean exists;
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "SELECT number FROM bout WHERE number = ? AND title IS NOT NULL"
            );
            stmt.setLong(1, number);
            final ResultSet rset = stmt.executeQuery();
            try {
                if (rset.next()) {
                    exists = true;
                } else {
                    exists = false;
                }
            } finally {
                rset.close();
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#checkBoutExistence(#%d): retrieved %b [%dms]",
            number,
            exists,
            System.currentTimeMillis() - start
        );
        return exists;
    }

    /**
     * New bout was just started.
     * @param number Number of the bout just started
     * @throws SQLException If some SQL problem inside
     */
    @Operation("started-new-bout")
    public void startedNewBout(final Long number) throws SQLException {
        this.changedBoutTitle(number, "");
    }

    /**
     * Get list of messages that belong to the specified bout.
     * @param bout Number of the bout to work with
     * @return List of message numbers
     * @throws SQLException If some SQL problem inside
     */
    @Operation("get-bout-messages")
    public Long[] getBoutMessages(final Long bout) throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        final List<Long> numbers = new ArrayList<Long>();
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                // @checkstyle LineLength (1 line)
                "SELECT number FROM message WHERE bout = ? AND date IS NOT NULL ORDER BY date"
            );
            stmt.setLong(1, bout);
            final ResultSet rset = stmt.executeQuery();
            try {
                while (rset.next()) {
                    numbers.add(rset.getLong(1));
                }
            } finally {
                rset.close();
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#getBoutMessages(#%d): retrieved %d message number(s) [%dms]",
            bout,
            numbers.size(),
            System.currentTimeMillis() - start
        );
        return numbers.toArray(new Long[]{});
    }

    /**
     * Get bout title.
     * @param number Number of bout
     * @return The title
     * @throws SQLException If some SQL problem inside
     */
    @Operation("get-bout-title")
    public String getBoutTitle(final Long number) throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        String title;
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "SELECT title FROM bout WHERE number = ?"
            );
            stmt.setLong(1, number);
            final ResultSet rset = stmt.executeQuery();
            try {
                if (!rset.next()) {
                    throw new IllegalArgumentException(
                        String.format(
                            "Bout #%d not found, can't read title",
                            number
                        )
                    );
                }
                title = rset.getString(1);
            } finally {
                rset.close();
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#getBoutTitle(%d): retrieved '%s' [%dms]",
            number,
            title,
            System.currentTimeMillis() - start
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
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "UPDATE bout SET title = ? WHERE number = ?"
            );
            stmt.setString(1, title);
            stmt.setLong(2, number);
            final int updated = stmt.executeUpdate();
            if (updated != 1) {
                throw new SQLException(
                    String.format(
                        "Bout #%d not found, title can't be changed",
                        number
                    )
                );
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#changedBoutTitle(%d, '%s'): updated [%dms]",
            number,
            title,
            System.currentTimeMillis() - start
        );
    }

}
