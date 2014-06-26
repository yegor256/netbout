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

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.Outcome;
import com.jcabi.urn.URN;
import com.netbout.spi.Aliases;
import com.netbout.spi.Friend;
import com.netbout.spi.User;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Cached Netbout user.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 2.4
 */
@Immutable
@ToString
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = { "sql", "urn" })
final class MkUser implements User {

    /**
     * SQL data source provider.
     */
    private final transient Sql sql;

    /**
     * His URN.
     */
    private final transient URN urn;

    /**
     * Public ctor.
     * @param src Source
     * @param name URN of user
     */
    MkUser(final Sql src, final URN name) {
        this.sql = src;
        this.urn = name;
    }

    @Override
    @Cacheable
    public Aliases aliases() {
        return new MkAliases(this.sql, this.urn);
    }

    @Override
    public Iterable<Friend> friends(final String text) throws IOException {
        try {
            return new JdbcSession(this.sql.source())
                .sql("SELECT name FROM alias WHERE name = ?")
                .set(text)
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
                                        MkUser.this.sql,
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
