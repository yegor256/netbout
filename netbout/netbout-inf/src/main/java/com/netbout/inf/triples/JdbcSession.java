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
package com.netbout.inf.triples;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.dbutils.DbUtils;

/**
 * Universal SQL DB manipulator.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class JdbcSession {

    /**
     * Connection to use.
     */
    private final transient Connection conn;

    /**
     * Table names.
     */
    private final transient List<String> tables = new LinkedList<String>();

    /**
     * The query to use.
     */
    private transient String query;

    /**
     * Arguments.
     */
    private final transient List<Object> args = new LinkedList<Object>();

    /**
     * Handler or ResultSet.
     * @param <T> Type of expected result
     */
    public interface Handler<T> {
        /**
         * Process the result set and return some value.
         * @param rset The result set to process
         * @return The result
         * @throws SQLException If something goes wrong inside
         */
        T handle(ResultSet rset) throws SQLException;
    }

    /**
     * Public ctor.
     * @param connection The connection to use
     */
    public JdbcSession(final Connection connection) {
        try {
            this.conn = connection;
            this.conn.setAutoCommit(true);
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
    public JdbcSession sql(final String sql) {
        this.query = sql;
        this.args.clear();
        return this;
    }

    /**
     * Use this table name.
     * @param name The name of the table
     * @return This object
     */
    public JdbcSession table(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("TABLE can't be NULL");
        }
        this.tables.add(name);
        return this;
    }

    /**
     * Set new param for the query.
     * @param value The value to add
     * @return This object
     */
    public JdbcSession set(final Object value) {
        if (value == null) {
            throw new IllegalArgumentException("COLUMN can't be NULL");
        }
        this.args.add(value);
        return this;
    }

    /**
     * Commit it.
     * @return This object
     */
    public JdbcSession commit() {
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
     */
    public void insert() {
        this.run(
            null,
            new Fetcher() {
                @Override
                public ResultSet fetch(final PreparedStatement stmt)
                    throws SQLException {
                    stmt.execute();
                    return null;
                }
            }
        );
    }

    /**
     * Make DELETE/UPDATE/CREATE/etc request.
     */
    public void execute() {
        this.run(
            null,
            new Fetcher() {
                @Override
                public ResultSet fetch(final PreparedStatement stmt)
                    throws SQLException {
                    stmt.executeUpdate();
                    return null;
                }
            }
        );
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
        T result = null;
        try {
            final PreparedStatement stmt = this.conn.prepareStatement(
                this.tablize(this.query)
            );
            try {
                this.parametrize(stmt);
                final ResultSet rset = fetcher.fetch(stmt);
                if (handler != null) {
                    result = handler.handle(rset);
                }
            } finally {
                DbUtils.closeQuietly(stmt);
            }
        } catch (SQLException ex) {
            throw new IllegalArgumentException(ex);
        } finally {
            DbUtils.closeQuietly(this.conn);
        }
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
            if (arg instanceof Long) {
                stmt.setLong(pos, (Long) arg);
            } else if (arg instanceof byte[]) {
                stmt.setBytes(pos, (byte[]) arg);
            } else {
                throw new IllegalArgumentException();
            }
            pos += 1;
        }
    }

    /**
     * Replace markers with table names.
     * @param sql The statement to tablize
     * @return The prepared text
     */
    private String tablize(final String sql) {
        String result = sql;
        for (int pos = 0; pos < this.tables.size(); ++pos) {
            result = result.replace(
                String.format("%%table-%d%%", pos + 1),
                String.format("\"%s\"", this.tables.get(pos))
            );
        }
        return result;
    }

}
