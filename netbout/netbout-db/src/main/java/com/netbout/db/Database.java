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
package com.netbout.db;

import com.jcabi.log.Logger;
import com.jolbox.bonecp.BoneCPDataSource;
import com.rexsl.core.Manifests;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

/**
 * Database-related utility class.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class Database {

    /**
     * Singleton instance.
     */
    private static final Database INSTANCE = new Database();

    /**
     * Datasource to use.
     */
    private final transient BoneCPDataSource src;

    /**
     * Protected ctor (for the sake of testability, but it should be FINAL,
     * of course).
     */
    private Database() {
        this.src = new BoneCPDataSource();
        this.src.setDriverClass(Manifests.read("Netbout-JdbcDriver"));
        this.src.setJdbcUrl(Manifests.read("Netbout-JdbcUrl"));
        this.src.setUsername(Manifests.read("Netbout-JdbcUser"));
        this.src.setPassword(Manifests.read("Netbout-JdbcPassword"));
        // @checkstyle MagicNumber (1 line)
        this.src.setMaxConnectionsPerPartition(50);
        this.liquibase();
        Logger.info(Database.class, "#Database(): ready");
    }

    /**
     * Convenient method to get a new JDBC connection.
     * @return New JDBC connection
     */
    public static DataSource source() {
        return Database.INSTANCE.src;
    }

    /**
     * Update DB schema to the latest version, with Liquibase.
     */
    private void liquibase() {
        Connection conn;
        try {
            conn = this.src.getConnection();
            try {
                new Liquibase(
                    "com/netbout/db/liquibase.xml",
                    new ClassLoaderResourceAccessor(),
                    new JdbcConnection(conn)
                ).update("netbout");
            } catch (liquibase.exception.LiquibaseException ex) {
                throw new IllegalStateException(ex);
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
