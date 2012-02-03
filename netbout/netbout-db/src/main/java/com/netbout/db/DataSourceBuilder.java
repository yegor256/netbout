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

import com.rexsl.core.Manifests;
import com.ymock.util.Logger;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DataSourceConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * Builder of a datasource.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class DataSourceBuilder {

    /**
     * Create and return JDBC data source.
     * @return The data source
     */
    public DataSource build() {
        final PoolableConnectionFactory factory = new PoolableConnectionFactory(
            this.factory(),
            new GenericObjectPool(null),
            null,
            "SELECT name FROM identity WHERE name = ''",
            false,
            true
        );
        final DataSource src = new PoolingDataSource(factory.getPool());
        Logger.info(
            this,
            "#datasource(): created %[type]s",
            src
        );
        return src;
    }

    /**
     * Create and return connection factory.
     * @return The connection factory
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private ConnectionFactory factory() {
        final long start = System.currentTimeMillis();
        final String driver = Manifests.read("Netbout-JdbcDriver");
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
        ConnectionFactory factory;
        try {
            factory = new DataSourceConnectionFactory(
                BasicDataSourceFactory.createDataSource(this.props())
            );
        // @checkstyle IllegalCatch (1 line)
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        Logger.info(
            this,
            "#factory(): created with '%s' [%dms]",
            driver,
            System.currentTimeMillis() - start
        );
        return factory;
    }

    /**
     * Properties for data source factory.
     * @return The properties
     * @see <a href="http://commons.apache.org/dbcp/configuration.html">DBCP configuration</a>
     */
    private Properties props() {
        final Properties props = new Properties();
        props.setProperty("url", Manifests.read("Netbout-JdbcUrl"));
        props.setProperty("username", Manifests.read("Netbout-JdbcUser"));
        props.setProperty("password", Manifests.read("Netbout-JdbcPassword"));
        props.setProperty("testWhileIdle", Boolean.TRUE.toString());
        props.setProperty("testOnBorrow", Boolean.TRUE.toString());
        props.setProperty("testOnReturn", Boolean.TRUE.toString());
        props.setProperty("maxActive", "8");
        props.setProperty("minEvictableIdleTimeMillis", "1800000");
        props.setProperty("timeBetweenEvictionRunsMillis", "1800001");
        props.setProperty("numTestsPerEvictionRun", "3");
        return props;
    }

}
