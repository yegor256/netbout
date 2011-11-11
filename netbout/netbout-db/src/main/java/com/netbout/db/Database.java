/**
 * Copyright (c) 2009-2011, netBout.com
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
import java.sql.SQLException;
import javax.sql.DataSource;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;

/**
 * Database-related utility class.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class Database {

    /**
     * Datasource to use.
     */
    private static final DataSource SOURCE = Database.datasource();

    /**
     * Read next bout number.
     * @return Next bout number
     * @throws SQLException If some SQL error
     */
    public static Connection connection() throws SQLException {
        return Database.datasource().getConnection();
    }

    /**
     * Create and return JDBC data source.
     * @return The data source
     */
    private static DataSource datasource() {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
        final PoolableConnectionFactory factory = new PoolableConnectionFactory(
            new DriverManagerConnectionFactory("jdbc:hsqldb:mem:testdb", "sa", ""),
            new GenericObjectPool(null),
            null,
            "xxx",
            // "SELECT 1 from INFORMATION_SCHEMA.SYSTEM_USERS",
            true,
            false,
            Connection.TRANSACTION_NONE
        );
        DataSource source = new PoolingDataSource(factory.getPool());
        Database.update(source);
        return source;
    }

    /**
     * Update DB schema.
     * @param The data source
     */
    private static void update(final DataSource source) {
        try {
            final Liquibase liquibase = new Liquibase(
                Database.class.getResource("liquibase.xml").getFile(),
                new ClassLoaderResourceAccessor(),
                new JdbcConnection(source.getConnection())
            );
            liquibase.validate();
            if (liquibase.isSafeToRunMigration()) {
                liquibase.update(1, "test");
            }
        } catch (liquibase.exception.LiquibaseException ex) {
            throw new IllegalStateException(ex);
        } catch (java.sql.SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
