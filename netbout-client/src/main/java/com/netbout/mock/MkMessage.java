/**
 * Copyright (c) 2009-2014, netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netbout Inc. located at www.netbout.com.
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
package com.netbout.mock;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.SingleOutcome;
import com.netbout.spi.Message;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Cached message.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.4
 */
@Immutable
@ToString
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = { "sql", "bout", "num" })
final class MkMessage implements Message {

    /**
     * SQL data source provider.
     */
    private final transient Sql sql;

    /**
     * Bout.
     */
    private final transient long bout;

    /**
     * Message number.
     */
    private final transient long num;

    /**
     * Public ctor.
     * @param src Source
     * @param bot Bout number
     * @param number Message number
     */
    MkMessage(final Sql src, final long bot, final long number) {
        this.sql = src;
        this.bout = bot;
        this.num = number;
    }

    @Override
    public long number() {
        return this.num;
    }

    @Override
    public Date date() throws IOException {
        final DateFormat format = new SimpleDateFormat(
            "yyyy-MM-DD HH:mm:ss.sss", Locale.ENGLISH
        );
        try {
            return format.parse(
                new JdbcSession(this.sql.source())
                    // @checkstyle LineLength (1 line)
                    .sql("SELECT date FROM message WHERE bout = ? AND number = ?")
                    .set(this.bout)
                    .set(this.num)
                    .select(new SingleOutcome<>(String.class))
            );
        } catch (final SQLException | ParseException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public String text() throws IOException {
        try {
            return new JdbcSession(this.sql.source())
                .sql("SELECT text FROM message WHERE bout = ? AND number = ?")
                .set(this.bout)
                .set(this.num)
                .select(new SingleOutcome<String>(String.class));
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public String author() throws IOException {
        try {
            return new JdbcSession(this.sql.source())
                .sql("SELECT author FROM message WHERE bout = ? AND number = ?")
                .set(this.bout)
                .set(this.num)
                .select(new SingleOutcome<String>(String.class));
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
}
