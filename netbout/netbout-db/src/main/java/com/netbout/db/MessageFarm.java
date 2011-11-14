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
 * Messages manipulations.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Farm
public final class MessageFarm {

    /**
     * New message added to the bout.
     * @param bout The bout
     * @param date When this message was posted
     * @throws SQLException If some SQL problem inside
     */
    @Operation("added-bout-message")
    public void addedBoutMessage(final Long bout, final Long date)
        throws SQLException {
        final Connection conn = Database.connection();
        Long number;
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO message (bout, date) VALUES (?, ?)"
            );
            stmt.setLong(1, bout);
            stmt.setLong(2, date);
            stmt.execute();
            final ResultSet rset = stmt.getGeneratedKeys();
            rset.next();
            rset.getLong(1);
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#addedBoutMessage(#%d, %d): added",
            bout,
            date
        );
    }

    /**
     * Get list of dates of all bout messages.
     * @param bout The bout where it happened
     * @return List of dates
     * @throws SQLException If some SQL problem inside
     */
    @Operation("get-bout-message-dates")
    public List<Long> getBoutMessageDates(final Long bout) throws SQLException {
        final Connection conn = Database.connection();
        final List<Long> dates = new ArrayList<Long>();
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "SELECT date FROM message WHERE bout = ? ORDER BY date"
            );
            stmt.setLong(1, bout);
            final ResultSet rset = stmt.executeQuery();
            while (rset.next()) {
                dates.add(rset.getLong(1));
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#getBoutMessageDates(#%d): retrieved %d dates",
            bout,
            dates.size()
        );
        return dates;
    }

    /**
     * Get message author.
     * @param bout The bout where it happened
     * @param date When this message was posted
     * @return Name of the author
     * @throws SQLException If some SQL problem inside
     */
    @Operation("get-message-author")
    public String getMessageAuthor(final Long bout, final Long date)
        throws SQLException {
        final Connection conn = Database.connection();
        String author;
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "SELECT author FROM message WHERE bout = ? AND date = ?"
            );
            stmt.setLong(1, bout);
            stmt.setLong(2, date);
            final ResultSet rset = stmt.executeQuery();
            rset.next();
            author = rset.getString(1);
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#getMessageAuthor(#%d, %d): retrieved '%s'",
            bout,
            date,
            author
        );
        return author;
    }

    /**
     * Changed message author.
     * @param bout The bout where it happened
     * @param date When this message was posted
     * @param author The author of the message to change
     * @throws SQLException If some SQL problem inside
     */
    @Operation("changed-message-author")
    public void changedMessageAuthor(final Long bout, final Long date,
        final String author) throws SQLException {
        final Connection conn = Database.connection();
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "UPDATE message SET author = ? WHERE bout = ? AND date = ?"
            );
            stmt.setString(1, author);
            stmt.setLong(2, bout);
            stmt.setLong(3, date);
            final int updated = stmt.executeUpdate();
            if (updated != 1) {
                throw new SQLException("Bout not found");
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#changedMessageAuthor(#%d, %d, '%s'): updated",
            bout,
            date,
            author
        );
    }

    /**
     * Get message text.
     * @param bout The bout where it happened
     * @param date When this message was posted
     * @return Text of the message
     * @throws SQLException If some SQL problem inside
     */
    @Operation("get-message-text")
    public String getMessageText(final Long bout, final Long date)
        throws SQLException {
        final Connection conn = Database.connection();
        String text;
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "SELECT text FROM message WHERE bout = ? AND date = ?"
            );
            stmt.setLong(1, bout);
            stmt.setLong(2, date);
            final ResultSet rset = stmt.executeQuery();
            rset.next();
            text = rset.getString(1);
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#getMessageText(#%d, %d): retrieved '%s'",
            bout,
            date,
            text
        );
        return text;
    }

    /**
     * Changed message text.
     * @param bout The bout where it happened
     * @param date When this message was posted
     * @param text The text to set
     * @throws SQLException If some SQL problem inside
     */
    @Operation("changed-message-text")
    public void changedMessageText(final Long bout, final Long date,
        final String text) throws SQLException {
        final Connection conn = Database.connection();
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "UPDATE message SET text = ? WHERE bout = ? AND date = ?"
            );
            stmt.setString(1, text);
            stmt.setLong(2, bout);
            stmt.setLong(3, date);
            final int updated = stmt.executeUpdate();
            if (updated != 1) {
                throw new SQLException("Bout not found, can't change msg text");
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#changedMessageText(#%d, %d, '%s'): updated",
            bout,
            date,
            text
        );
    }

}
