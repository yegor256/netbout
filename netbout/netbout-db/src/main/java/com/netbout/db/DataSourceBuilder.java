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
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;

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
     * @checkstyle MagicNumber (30 lines)
     * @checkstyle ExecutableStatementCount (30 lines)
     */
    public DataSource build() {
        final String url = Manifests.read("Netbout-JdbcUrl");
        final BasicDataSource data = new BasicDataSource();
        data.setDriverClassName(Manifests.read("Netbout-JdbcDriver"));
        data.setUrl(url);
        data.setUsername(Manifests.read("Netbout-JdbcUser"));
        data.setPassword(Manifests.read("Netbout-JdbcPassword"));
        data.setMaxActive(8);
        data.setMaxIdle(8);
        data.setInitialSize(2);
        data.setMaxWait(30000);
        data.setPoolPreparedStatements(true);
        data.setMaxOpenPreparedStatements(10);
        data.setTestOnBorrow(true);
        data.setTestOnReturn(true);
        data.setTestWhileIdle(true);
        data.setTimeBetweenEvictionRunsMillis(5000);
        data.setNumTestsPerEvictionRun(5);
        data.setMinEvictableIdleTimeMillis(15000);
        data.setDefaultAutoCommit(true);
        data.setDefaultReadOnly(false);
        Logger.info(
            this,
            "#datasource(): created %[type]s for %s",
            data,
            url
        );
        return data;
    }

}
