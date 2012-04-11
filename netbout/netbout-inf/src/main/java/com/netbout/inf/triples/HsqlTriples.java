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

import com.jolbox.bonecp.BoneCPDataSource;
import com.netbout.spi.Message;
import com.ymock.util.Logger;
import java.io.Closeable;
import java.io.File;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import javax.sql.DataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.SerializationUtils;

/**
 * Triples with HSQL DB.
 *
 * <p>The class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class HsqlTriples implements Triples {

    /**
     * Names of tables already created in the DB.
     */
    private final transient Set<String> tables =
        new ConcurrentSkipListSet<String>();

    /**
     * Preserved result sets.
     */
    private final transient ConcurrentHashMap<Long, ResultSet> rsets =
        new ConcurrentHashMap<Long, ResultSet>();

    /**
     * The connection to DB.
     */
    private final transient DataSource source;

    /**
     * Public ctor.
     * @param dir Where to keep data
     */
    public HsqlTriples(final File dir) {
        this.source = HsqlTriples.datasource(dir);
        Logger.debug(this, "#HsqlTriples(%s): instantiated", dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws java.io.IOException {
        Logger.debug(this, "#close(): closed");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> void put(final Long number, final String name, final T value) {
        if (value instanceof Long) {
            this.session(name)
                .sql("INSERT INTO %table-1% VALUES (?, ?, ?)")
                .table(name)
                .set(number)
                .set(this.serialize(value))
                .set(value)
                .insert();
        } else {
            this.session(name)
                .sql("INSERT INTO %table-1% VALUES (?, ?, 0)")
                .table(name)
                .set(number)
                .set(this.serialize(value))
                .insert();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> boolean has(final Long number, final String name,
        final T value) {
        return this.session(name)
            .sql("SELECT * FROM %table-1% WHERE key=? AND value=? LIMIT 1")
            .table(name)
            .set(number)
            .set(this.serialize(value))
            .select(
                new JdbcSession.Handler<Boolean>() {
                    @Override
                    public Boolean handle(final ResultSet rset)
                        throws SQLException {
                        final boolean has = rset.next();
                        rset.close();
                        return has;
                    }
                }
            );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(final Long number, final String name)
        throws MissedTripleException {
        final T value = this.session(name)
            .sql("SELECT value FROM %table-1% WHERE key=? LIMIT 1")
            .table(name)
            .set(number)
            .select(
                new JdbcSession.Handler<T>() {
                    @Override
                    public T handle(final ResultSet rset) throws SQLException {
                        T val = null;
                        if (rset.next()) {
                            val = (T) HsqlTriples.deserialize(rset.getBytes(1));
                        }
                        rset.close();
                        return val;
                    }
                }
            );
        if (value == null) {
            throw new MissedTripleException(
                String.format(
                    "can't find %d in %s",
                    number,
                    name
                )
            );
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Iterator<T> all(final Long number, final String name) {
        return this.session(name)
            .sql("SELECT value FROM %table-1% WHERE key=?")
            .table(name)
            .set(number)
            .select(
                new JdbcSession.Handler<Iterator<T>>() {
                    @Override
                    public Iterator<T> handle(final ResultSet rset)
                        throws SQLException {
                        return new AbstractIterator<T>() {
                            @Override
                            public T fetch() {
                                T value = null;
                                try {
                                    if (rset.next()) {
                                        value = (T) HsqlTriples.deserialize(
                                            rset.getBytes(1)
                                        );
                                    }
                                } catch (SQLException ex) {
                                    throw new IllegalStateException(ex);
                                }
                                HsqlTriples.this.preserve(rset);
                                return value;
                            }
                        };
                    }
                }
            );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Iterator<Long> reverse(final String name, final T value) {
        return this.session(name)
            .sql("SELECT key FROM %table-1% WHERE value=? ORDER BY key DESC")
            .table(name)
            .set(HsqlTriples.serialize(value))
            .select(new LongHandler());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Iterator<Long> reverse(final String name, final String join,
        final T value) {
        return this.session(name)
            .sql("SELECT l.key FROM %table-1% AS l JOIN %table-2% AS r ON l.vnum = r.key WHERE r.value=? ORDER BY l.key DESC")
            .table(name)
            .table(join)
            .set(HsqlTriples.serialize(value))
            .select(new LongHandler());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear(final Long number, final String name) {
        this.session(name)
            .sql("DELETE FROM %table-1% WHERE key=?")
            .table(name)
            .set(number)
            .execute();
    }

    /**
     * Create new session.
     * @param name Name of the table to use
     * @return JDBC session
     */
    private JdbcSession session(final String name) {
        try {
            synchronized (this.tables) {
                if (!this.tables.contains(name)) {
                    new JdbcSession(this.source.getConnection()).sql(
                        "CREATE CACHED TABLE IF NOT EXISTS %table-1%"
                        + " (key BIGINT, value BINARY(1024), vnum BIGINT)"
                    ).table(name).execute();
                    this.tables.add(name);
                }
            }
            return new JdbcSession(this.source.getConnection());
        } catch (java.sql.SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Serialize this value into string.
     * @param value The value
     * @return Serialized
     */
    private static <T> byte[] serialize(final T value) {
        return SerializationUtils.serialize((Serializable) value);
    }

    /**
     * Deserialize this string into value.
     * @param bytes The data
     * @return De-serialized value
     */
    private static <T> T deserialize(final byte[] bytes) {
        return (T) SerializationUtils.deserialize(bytes);
    }

    /**
     * Create datasource in this directory.
     * @param dir Where to keep data
     * @return The datasource
     */
    private static DataSource datasource(final File dir) {
        final BoneCPDataSource src = new BoneCPDataSource();
        src.setDriverClass("org.hsqldb.jdbcDriver");
        src.setJdbcUrl(
            String.format(
                "jdbc:hsqldb:file:%s",
                new File(dir, "hsqldb.sql")
            )
        );
        src.setUsername("sa");
        src.setPassword("");
        src.setMaxConnectionsPerPartition(50);
        return src;
    }

    /**
     * Preserve this ResultSet for some time future.
     * @param rset The result set
     */
    private void preserve(final ResultSet rset) {
        synchronized (this.rsets) {
            Long now = System.currentTimeMillis();
            for (Long time : this.rsets.keySet()) {
                // @checkstyle MagicNumber (1 line)
                if (time < now - 10 * 1000) {
                    DbUtils.closeQuietly(this.rsets.get(time));
                    this.rsets.remove(time);
                }
            }
            while (this.rsets.containsKey(now)) {
                --now;
            }
            this.rsets.put(now, rset);
        }
    }

    /**
     * Long handler.
     */
    private final class LongHandler implements
        JdbcSession.Handler<Iterator<Long>> {
        @Override
        public Iterator<Long> handle(final ResultSet rset)
            throws SQLException {
            return new AbstractIterator<Long>() {
                @Override
                public Long fetch() {
                    Long number = null;
                    try {
                        if (rset.next()) {
                            number = rset.getLong(1);
                        }
                    } catch (SQLException ex) {
                        throw new IllegalStateException(ex);
                    }
                    HsqlTriples.this.preserve(rset);
                    return number;
                }
            };
        }
    }


}
