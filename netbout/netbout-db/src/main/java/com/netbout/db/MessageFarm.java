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

import com.netbout.spi.Urn;
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
 * Messages manipulations.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Farm
public final class MessageFarm {

    /**
     * Create new message in a bout and return its unique number.
     * @param bout The bout
     * @return The number of the message
     * @throws SQLException If some SQL problem inside
     */
    @Operation("create-bout-message")
    public Long createBoutMessage(final Long bout)
        throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        Long number;
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO message (bout) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS
            );
            stmt.setLong(1, bout);
            stmt.execute();
            final ResultSet rset = stmt.getGeneratedKeys();
            try {
                rset.next();
                number = rset.getLong(1);
            } finally {
                rset.close();
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#createBoutMessage(#%d): created message #%d [%dms]",
            bout,
            number,
            System.currentTimeMillis() - start
        );
        return number;
    }

    /**
     * Check message existence in the bout.
     * @param bout Bout number to check
     * @param msg Message number to check
     * @return It exists?
     * @throws SQLException If some SQL problem inside
     */
    @Operation("check-message-existence")
    public Boolean checkMessageExistence(final Long bout, final Long msg)
        throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        Boolean exists;
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "SELECT number FROM message WHERE number = ? AND bout = ?"
            );
            stmt.setLong(1, msg);
            stmt.setLong(2, bout);
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
            "#checkMessageExistence(#%d, #%d): retrieved %b [%dms]",
            bout,
            msg,
            exists,
            System.currentTimeMillis() - start
        );
        return exists;
    }

    /**
     * Get list of numbers of all bout messages.
     * @param bout The bout where it happened
     * @return List of numbers
     * @throws SQLException If some SQL problem inside
     */
    @Operation("get-bout-messages")
    public List<Long> getBoutMessages(final Long bout) throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        final List<Long> numbers = new ArrayList<Long>();
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "SELECT number FROM message WHERE bout = ? AND date IS NOT NULL"
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
        return numbers;
    }

    /**
     * Get message date.
     * @param number Number of the message
     * @return The date
     * @throws SQLException If some SQL problem inside
     */
    @Operation("get-message-date")
    public Date getMessageDate(final Long number)
        throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        Date date;
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "SELECT date FROM message WHERE number = ?"
            );
            stmt.setLong(1, number);
            final ResultSet rset = stmt.executeQuery();
            try {
                if (!rset.next()) {
                    throw new IllegalArgumentException(
                        String.format(
                            "Message #%d not found, can't get date",
                            number
                        )
                    );
                }
                date = Utc.getTimestamp(rset, 1);
            } finally {
                rset.close();
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#getMessageDate(#%d): retrieved '%s' [%dms]",
            number,
            date,
            System.currentTimeMillis() - start
        );
        return date;
    }

    /**
     * Changed message date.
     * @param number The bout where it happened
     * @param date The date of the message to change
     * @throws SQLException If some SQL problem inside
     */
    @Operation("changed-message-date")
    public void changedMessageDate(final Long number, final Date date)
        throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "UPDATE message SET date = ? WHERE number = ?"
            );
            Utc.setTimestamp(stmt, 1, date);
            stmt.setLong(2, number);
            final int updated = stmt.executeUpdate();
            if (updated != 1) {
                throw new SQLException(
                    String.format(
                        "Message #%d not found, can't set date to '%s'",
                        number,
                        date
                    )
                );
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#changedMessageDate(#%d, '%s'): updated [%dms]",
            number,
            date,
            System.currentTimeMillis() - start
        );
    }

    /**
     * Get message author.
     * @param number Number of the message
     * @return Name of the author
     * @throws SQLException If some SQL problem inside
     */
    @Operation("get-message-author")
    public Urn getMessageAuthor(final Long number)
        throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        String author;
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "SELECT author FROM message WHERE number = ?"
            );
            stmt.setLong(1, number);
            final ResultSet rset = stmt.executeQuery();
            try {
                if (!rset.next()) {
                    throw new IllegalArgumentException(
                        String.format(
                            "Message #%d not found, can't get author",
                            number
                        )
                    );
                }
                author = rset.getString(1);
            } finally {
                rset.close();
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#getMessageAuthor(#%d): retrieved '%s' [%dms]",
            number,
            author,
            System.currentTimeMillis() - start
        );
        return Urn.create(author);
    }

    /**
     * Changed message author.
     * @param number The bout where it happened
     * @param author The author of the message to change
     * @throws SQLException If some SQL problem inside
     */
    @Operation("changed-message-author")
    public void changedMessageAuthor(final Long number, final Urn author)
        throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "UPDATE message SET author = ? WHERE number = ?"
            );
            stmt.setString(1, author.toString());
            stmt.setLong(2, number);
            final int updated = stmt.executeUpdate();
            if (updated != 1) {
                throw new SQLException("Message not found");
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#changedMessageAuthor(#%d, '%s'): updated [%dms]",
            number,
            author,
            System.currentTimeMillis() - start
        );
    }

    /**
     * Get message text.
     * @param number The number of the message
     * @return Text of the message
     * @throws SQLException If some SQL problem inside
     */
    @Operation("get-message-text")
    public String getMessageText(final Long number)
        throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        String text;
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "SELECT text FROM message WHERE number = ?"
            );
            stmt.setLong(1, number);
            final ResultSet rset = stmt.executeQuery();
            try {
                if (!rset.next()) {
                    throw new IllegalArgumentException(
                        String.format(
                            "Message #%d not found, can't read text",
                            number
                        )
                    );
                }
                text = rset.getString(1);
            } finally {
                rset.close();
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#getMessageText(#%d): retrieved '%s' [%dms]",
            number,
            text,
            System.currentTimeMillis() - start
        );
        return text;
    }

    /**
     * Changed message text.
     * @param number The bout where it happened
     * @param text The text to set
     * @throws SQLException If some SQL problem inside
     */
    @Operation("changed-message-text")
    public void changedMessageText(final Long number,
        final String text) throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "UPDATE message SET text = ? WHERE number = ?"
            );
            stmt.setString(1, text);
            stmt.setLong(2, number);
            final int updated = stmt.executeUpdate();
            if (updated != 1) {
                throw new SQLException("Message not found, can't save text");
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#changedMessageText(#%d, '%s'): updated [%dms]",
            number,
            text,
            System.currentTimeMillis() - start
        );
    }

}
