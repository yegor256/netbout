/**
 * Copyright (c) 2009-2015, netbout.com
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
import com.netbout.spi.Friend;
import com.netbout.spi.Friends;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Cached friends.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.4
 */
@Immutable
@ToString
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = { "sql", "bout" })
final class MkFriends implements Friends {

    /**
     * SQL data source provider.
     */
    private final transient Sql sql;

    /**
     * Bout.
     */
    private final transient long bout;

    /**
     * Public ctor.
     * @param src Source
     * @param bot Bout number
     */
    MkFriends(final Sql src, final long bot) {
        this.sql = src;
        this.bout = bot;
    }

    @Override
    public void invite(final String friend) throws IOException {
        try {
            new JdbcSession(this.sql.source())
                .sql("INSERT INTO friend (bout, alias) VALUES (?, ?)")
                .set(this.bout)
                .set(friend)
                .insert(Outcome.VOID);
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void kick(final String friend) throws IOException {
        try {
            new JdbcSession(this.sql.source())
                .sql("DELETE FROM friend WHERE bout = ? AND alias = ?")
                .set(this.bout)
                .set(friend)
                .update(Outcome.VOID);
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public Iterable<Friend> iterate() throws IOException {
        try {
            return new JdbcSession(this.sql.source())
                .sql("SELECT alias FROM friend WHERE bout = ?")
                .set(this.bout)
                .select(
                    new Outcome<Iterable<Friend>>() {
                        @Override
                        public Iterable<Friend> handle(final ResultSet rset,
                            final Statement stmt) throws SQLException {
                            final Collection<Friend> list =
                                new LinkedList<Friend>();
                            while (rset.next()) {
                                list.add(
                                    new MkFriend(
                                        MkFriends.this.sql,
                                        rset.getString(1)
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
}
