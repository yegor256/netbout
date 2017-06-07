/**
 * Copyright (c) 2009-2017, netbout.com
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
import com.jcabi.jdbc.SingleOutcome;
import com.netbout.spi.Bout;
import com.netbout.spi.Inbox;
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
 * Cached inbox.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 2.4
 */
@Immutable
@ToString
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = { "sql", "self" })
final class MkInbox implements Inbox {

    /**
     * SQL data source provider.
     */
    private final transient Sql sql;

    /**
     * Self alias.
     */
    private final transient String self;

    /**
     * Public ctor.
     * @param src Source
     * @param name Alias
     */
    MkInbox(final Sql src, final String name) {
        this.sql = src;
        this.self = name;
    }

    @Override
    public long start() throws IOException {
        try {
            final Long number = new JdbcSession(this.sql.source())
                .sql("INSERT INTO bout (title) VALUES (?)")
                .set("untitled")
                .insert(new SingleOutcome<Long>(Long.class));
            this.bout(number).friends().invite(this.self);
            return number;
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public long unread() {
        return 0L;
    }

    @Override
    public Bout bout(final long number) throws Inbox.BoutNotFoundException {
        final boolean exists;
        try {
            exists = new JdbcSession(this.sql.source())
                .sql("SELECT number FROM bout WHERE number = ?")
                .set(number)
                .select(Outcome.NOT_EMPTY);
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex);
        }
        if (!exists) {
            throw new Inbox.BoutNotFoundException(number);
        }
        return new MkBout(this.sql, number, this.self);
    }

    @Override
    public Pageable<Bout> jump(final long number) {
        return this;
    }

    @Override
    public Iterable<Bout> iterate() throws IOException {
        try {
            return new JdbcSession(this.sql.source())
                // @checkstyle LineLength (1 line)
                .sql("SELECT b.number FROM bout b JOIN friend f ON b.number = f.bout WHERE f.alias = ?")
                .set(this.self)
                .select(
                    new Outcome<Iterable<Bout>>() {
                        @Override
                        public Iterable<Bout> handle(final ResultSet rset,
                            final Statement stmt) throws SQLException {
                            final Collection<Bout> list = new LinkedList<>();
                            while (rset.next()) {
                                list.add(
                                    new MkBout(
                                        MkInbox.this.sql,
                                        rset.getLong(1),
                                        MkInbox.this.self
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
    public Iterable<Bout> search(final String term) throws IOException {
        final List<Bout> result = new ArrayList<>(16);
        for (final Bout bout : this.iterate()) {
            if (bout.title().contains(term)) {
                result.add(bout);
            } else if (bout.messages().search(term).iterator().hasNext()) {
                result.add(bout);
            }
        }
        return result;
    }
}
