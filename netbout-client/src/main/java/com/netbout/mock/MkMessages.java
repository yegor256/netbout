/**
 * Copyright (c) 2009-2016, netbout.com
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
import com.jcabi.jdbc.Outcome;
import com.netbout.spi.Message;
import com.netbout.spi.Messages;
import com.netbout.spi.Pageable;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Cached messages.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.4
 */
@Immutable
@ToString
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = { "sql", "bout", "self" })
final class MkMessages implements Messages {

    /**
     * SQL data source provider.
     */
    private final transient Sql sql;

    /**
     * Bout.
     */
    private final transient long bout;

    /**
     * Self alias.
     */
    private final transient String self;

    /**
     * Public ctor.
     * @param src Source
     * @param bot Bout number
     * @param slf Self alias
     */
    MkMessages(final Sql src, final long bot, final String slf) {
        this.sql = src;
        this.bout = bot;
        this.self = slf;
    }

    @Override
    public void post(final String text) throws IOException {
        try {
            new JdbcSession(this.sql.source())
                // @checkstyle LineLength (1 line)
                .sql("INSERT INTO message (bout, text, author) VALUES (?, ?, ?)")
                .set(this.bout)
                .set(text)
                .set(this.self)
                .insert(Outcome.VOID);
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
        new TouchBout(this.sql, this.bout).act();
    }

    @Override
    public long unread() {
        return 0L;
    }

    @Override
    public Pageable<Message> jump(final long number) {
        return this;
    }

    @Override
    public Iterable<Message> iterate() throws IOException {
        try {
            return new JdbcSession(this.sql.source())
                .sql("SELECT number FROM message WHERE bout = ?")
                .set(this.bout)
                .select(
                    new Outcome<Iterable<Message>>() {
                        @Override
                        public Iterable<Message> handle(final ResultSet rset,
                            final Statement stmt) throws SQLException {
                            final Collection<Message> list =
                                new LinkedList<Message>();
                            while (rset.next()) {
                                list.add(
                                    new MkMessage(
                                        MkMessages.this.sql,
                                        MkMessages.this.bout,
                                        rset.getLong(1)
                                    )
                                );
                            }
                            return list;
                        }
                    }
                );
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public Iterable<Message> search(final String term) throws IOException {
        final List<Message> result = new ArrayList<>(16);
        for (final Message message : this.iterate()) {
            if (message.text().contains(term)) {
                result.add(message);
            }
        }
        return result;
    }
}
