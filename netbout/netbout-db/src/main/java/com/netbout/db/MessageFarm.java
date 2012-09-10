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
import com.netbout.spi.Urn;
import com.netbout.spi.cpa.Farm;
import com.netbout.spi.cpa.Operation;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Messages manipulations.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Farm
@SuppressWarnings("PMD.TooManyMethods")
public final class MessageFarm {

    /**
     * Create new message in a bout and return its unique number.
     * @param bout The bout
     * @return The number of the message
     */
    @Operation("create-bout-message")
    public Long createBoutMessage(final Long bout) {
        return new JdbcSession(Database.source())
            .sql("INSERT INTO message (bout, date) VALUES (?, ?)")
            .set(bout)
            .set(new Utc())
            .insert(new SingleHandler<Long>(Long.class));
    }

    /**
     * Get chunk of message numbers.
     * @param since Number of the message, since when we need more numbers
     * @param length Maximum length of the chunk
     * @return The list of numbers
     */
    @Operation("get-messages-chunk")
    public List<Long> getMessagesChunk(final Long since, final Long length) {
        return new JdbcSession(Database.source()).sql(
            // @checkstyle StringLiteralsConcatenation (2 lines)
            "SELECT number FROM message WHERE number > ?"
            + " ORDER BY number LIMIT ?"
        )
            .set(since)
            .set(length)
            .select(
                new JdbcSession.Handler<List<Long>>() {
                    @Override
                    public List<Long> handle(final ResultSet rset)
                        throws SQLException {
                        final List<Long> numbers = new LinkedList<Long>();
                        while (rset.next()) {
                            numbers.add(rset.getLong(1));
                        }
                        return numbers;
                    }
                }
            );
    }

    /**
     * Check message existence in the bout.
     * @param bout Bout number to check
     * @param msg Message number to check
     * @return It exists?
     */
    @Operation("check-message-existence")
    public Boolean checkMessageExistence(final Long bout, final Long msg) {
        return new JdbcSession(Database.source())
            .sql("SELECT number FROM message WHERE number = ? AND bout = ?")
            .set(msg)
            .set(bout)
            .select(new NotEmptyHandler());
    }

    /**
     * Get bout of message.
     * @param msg Message number to check
     * @return Number of bout or ZERO if such a message is not found
     */
    @Operation("get-bout-of-message")
    public Long getBoutOfMessage(final Long msg) {
        return new JdbcSession(Database.source())
            .sql("SELECT bout FROM message WHERE number = ?")
            .set(msg)
            .select(
                new JdbcSession.Handler<Long>() {
                    @Override
                    public Long handle(final ResultSet rset)
                        throws SQLException {
                        Long bout;
                        if (rset.next()) {
                            bout = rset.getLong(1);
                        } else {
                            bout = 0L;
                        }
                        return bout;
                    }
                }
            );
    }

    /**
     * Get message date.
     * @param number Number of the message
     * @return The date
     */
    @Operation("get-message-date")
    public Date getMessageDate(final Long number) {
        return new JdbcSession(Database.source())
            .sql("SELECT date FROM message WHERE number = ?")
            .set(number)
            .select(new SingleHandler<Utc>(Utc.class))
            .getDate();
    }

    /**
     * Changed message date.
     * @param number The bout where it happened
     * @param date The date of the message to change
     */
    @Operation("changed-message-date")
    public void changedMessageDate(final Long number, final Date date) {
        new JdbcSession(Database.source())
            .sql("UPDATE message SET date = ? WHERE number = ?")
            .set(new Utc(date))
            .set(number)
            .update();
    }

    /**
     * Get message author.
     * @param number Number of the message
     * @return Name of the author
     */
    @Operation("get-message-author")
    public Urn getMessageAuthor(final Long number) {
        return new JdbcSession(Database.source())
            .sql("SELECT author FROM message WHERE number = ?")
            .set(number)
            .select(
                new JdbcSession.Handler<Urn>() {
                    @Override
                    public Urn handle(final ResultSet rset)
                        throws SQLException {
                        if (!rset.next()) {
                            throw new IllegalArgumentException(
                                String.format(
                                    "Message #%d not found, can't get author",
                                    number
                                )
                            );
                        }
                        return Urn.create(rset.getString(1));
                    }
                }
            );
    }

    /**
     * Changed message author.
     * @param number The bout where it happened
     * @param author The author of the message to change
     */
    @Operation("changed-message-author")
    public void changedMessageAuthor(final Long number, final Urn author) {
        new JdbcSession(Database.source())
            .sql("UPDATE message SET author = ? WHERE number = ?")
            .set(author)
            .set(number)
            .update();
    }

    /**
     * Get message text.
     * @param number The number of the message
     * @return Text of the message
     */
    @Operation("get-message-text")
    public String getMessageText(final Long number) {
        return new JdbcSession(Database.source())
            .sql("SELECT text FROM message WHERE number = ?")
            .set(number)
            .select(new SingleHandler<String>(String.class));
    }

    /**
     * Changed message text.
     * @param number The bout where it happened
     * @param text The text to set
     */
    @Operation("changed-message-text")
    public void changedMessageText(final Long number, final String text) {
        new JdbcSession(Database.source())
            .sql("UPDATE message SET text = ? WHERE number = ?")
            .set(text)
            .set(number)
            .update();
    }

}
