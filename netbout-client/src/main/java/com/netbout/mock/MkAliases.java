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
import com.jcabi.jdbc.Outcome;
import com.jcabi.urn.URN;
import com.netbout.spi.Alias;
import com.netbout.spi.Aliases;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Cached aliases.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 2.4
 */
@Immutable
@ToString
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = { "sql", "urn" })
final class MkAliases implements Aliases {

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
    MkAliases(final Sql src, final URN name) {
        this.sql = src;
        this.urn = name;
    }

    @Override
    public String check(final String name) throws IOException {
        final boolean exists;
        try {
            exists = new JdbcSession(this.sql.source())
                .sql("SELECT COUNT(*) FROM alias WHERE name = ?")
                .set(name)
                .select(Outcome.NOT_EMPTY);
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
        final String msg;
        if (exists) {
            msg = String.format("alias '%s' occupied", name);
        } else {
            msg = "";
        }
        return msg;
    }

    @Override
    public void add(final String name) throws IOException {
        try {
            new JdbcSession(this.sql.source())
                .sql("INSERT INTO alias (name, urn, photo) VALUES (?, ?, ?)")
                .set(name)
                .set(this.urn.toString())
                .set(Alias.BLANK)
                .insert(Outcome.VOID);
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public Iterable<Alias> iterate() throws IOException {
        try {
            return new JdbcSession(this.sql.source())
                .sql("SELECT name FROM alias WHERE urn = ?")
                .set(this.urn.toString())
                .select(
                    new Outcome<Iterable<Alias>>() {
                        @Override
                        public Iterable<Alias> handle(final ResultSet rset,
                            final Statement stmt) throws SQLException {
                            final Collection<Alias> list =
                                new LinkedList<Alias>();
                            while (rset.next()) {
                                list.add(
                                    new MkAlias(
                                        MkAliases.this.sql,
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
