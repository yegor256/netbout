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

import com.ymock.util.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.dbutils.DbUtils;

/**
 * Universal SQL DB manipulator.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class DbSession {

    /**
     * Connection to use.
     */
    private final transient Connection conn;

    /**
     * Shall we close/autocommit automatically?
     */
    private final transient boolean auto;

    /**
     * The query to use.
     */
    private transient String query;

    /**
     * Arguments.
     */
    private final transient List<Object> args = new ArrayList<Object>();

    /**
     * Public ctor.
     * @param autocommit Shall we commit it automatically?
     */
    public DbSession(final boolean autocommit) {
        this.auto = autocommit;
        try {
            this.conn = Database.connection();
            this.conn.setAutoCommit(this.auto);
            this.conn.setTransactionIsolation(
                Connection.TRANSACTION_SERIALIZABLE
            );
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Use this SQL query.
     * @param sql The query to use
     * @return This object
     */
    public DbSession sql(final String sql) {
        this.query = sql;
        this.args.clear();
        return this;
    }

    /**
     * Set new param for the query.
     * @param value The value to add
     * @return This object
     */
    public DbSession set(final Object value) {
        this.args.add(value);
        return this;
    }

    /**
     * Commit it.
     * @return This object
     */
    public DbSession commit() {
        try {
            this.conn.commit();
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
        DbUtils.closeQuietly(this.conn);
        return this;
    }

    /**
     * Make INSERT request.
     * @param handler The handler or result
     * @return The result
     * @param <T> Type of response
     */
    public <T> T insert(final Handler<T> handler) {
        return this.run(
            handler,
            new Fetcher() {
                @Override
                public ResultSet fetch(final PreparedStatement stmt)
                    throws SQLException {
                    stmt.execute();
                    return stmt.getGeneratedKeys();
                }
            }
        );
    }

    /**
     * Make UPDATE request.
     * @return This object
     */
    public DbSession update() {
        this.run(
            new VoidHandler(),
            new Fetcher() {
                @Override
                public ResultSet fetch(final PreparedStatement stmt)
                    throws SQLException {
                    stmt.executeUpdate();
                    return null;
                }
            }
        );
        return this;
    }

    /**
     * Make UPDATE request.
     * @param handler The handler or result
     * @return The result
     * @param <T> Type of response
     */
    public <T> T select(final Handler<T> handler) {
        return this.run(
            handler,
            new Fetcher() {
                @Override
                public ResultSet fetch(final PreparedStatement stmt)
                    throws SQLException {
                    return stmt.executeQuery();
                }
            }
        );
    }

    /**
     * The fetcher.
     */
    private interface Fetcher {
        /**
         * Fetch result set from statement.
         * @param stmt The statement
         * @return The result set
         * @throws SQLException If some problem
         */
        ResultSet fetch(PreparedStatement stmt) throws SQLException;
    }

    /**
     * Run this handler, and this fetcher.
     * @param handler The handler or result
     * @param fetcher Fetcher of result set
     * @return The result
     * @param <T> Type of response
     * @checkstyle NestedTryDepth (40 lines)
     */
    @SuppressWarnings("PMD.CloseResource")
    private <T> T run(final Handler<T> handler, final Fetcher fetcher) {
        Database.log(this.query);
        final long start = System.nanoTime();
        T result;
        try {
            final PreparedStatement stmt = this.conn.prepareStatement(
                this.query,
                Statement.RETURN_GENERATED_KEYS
            );
            try {
                this.parametrize(stmt);
                final ResultSet rset = fetcher.fetch(stmt);
                try {
                    result = handler.handle(rset);
                } finally {
                    DbUtils.closeQuietly(rset);
                }
            } finally {
                DbUtils.closeQuietly(stmt);
            }
        } catch (SQLException ex) {
            if (this.auto) {
                DbUtils.closeQuietly(this.conn);
            } else {
                DbUtils.rollbackAndCloseQuietly(this.conn);
            }
            Logger.error(
                this,
                "#run(..): '%s':\n%[exception]s",
                this.query,
                ex
            );
            throw new IllegalArgumentException(ex);
        } finally {
            if (this.auto) {
                DbUtils.closeQuietly(this.conn);
            }
        }
        Logger.debug(
            this,
            "#run(): '%s' done in %[nano]s",
            this.query,
            System.nanoTime() - start
        );
        return result;
    }

    /**
     * Add params to the statement.
     * @param stmt The statement to parametrize
     * @throws SQLException If some problem
     */
    private void parametrize(final PreparedStatement stmt) throws SQLException {
        int pos = 1;
        for (Object arg : this.args) {
            if (arg == null) {
                stmt.setString(pos, null);
            } else if (arg instanceof Long) {
                stmt.setLong(pos, (Long) arg);
            } else if (arg instanceof Boolean) {
                stmt.setBoolean(pos, (Boolean) arg);
            } else if (arg instanceof Date) {
                Utc.setTimestamp(stmt, pos, (Date) arg);
            } else {
                stmt.setString(pos, arg.toString());
            }
            pos += 1;
        }
    }

}
