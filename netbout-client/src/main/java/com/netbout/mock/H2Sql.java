/**
 * Copyright (c) 2009-2016, netbout.com
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
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.h2.jdbcx.JdbcDataSource;

/**
 * Mock base.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.0
 */
@Immutable
@ToString
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = "file")
final class H2Sql implements Sql {

    /**
     * File with H2 database.
     */
    private final transient String file;

    /**
     * Public ctor.
     * @throws IOException If fails
     */
    H2Sql() throws IOException {
        final File tmp = File.createTempFile("netbout-", ".h2");
        tmp.deleteOnExit();
        this.file = tmp.getAbsolutePath();
        new File(String.format("%s.mv.db", this.file)).deleteOnExit();
        final String[] stmts = {
            // @checkstyle LineLength (5 lines)
            "CREATE TABLE alias (name VARCHAR, urn VARCHAR, photo VARCHAR, locale VARCHAR, email VARCHAR)",
            "CREATE TABLE bout (number BIGINT AUTO_INCREMENT, title VARCHAR, date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
            "CREATE TABLE message (number BIGINT AUTO_INCREMENT, bout BIGINT, text VARCHAR, author VARCHAR, date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
            "CREATE TABLE attachment (name VARCHAR, bout BIGINT, data VARCHAR, author VARCHAR, ctype VARCHAR, etag VARCHAR)",
            "CREATE TABLE friend (alias VARCHAR, bout BIGINT, subscription INTEGER )",
        };
        final JdbcSession session = new JdbcSession(this.source());
        for (final String stmt : stmts) {
            try {
                session.sql(stmt).execute();
            } catch (final SQLException ex) {
                throw new IOException(ex);
            }
        }
    }

    @Override
    public DataSource source() {
        final JdbcDataSource src = new JdbcDataSource();
        src.setURL(String.format("jdbc:h2:%s", this.file));
        src.setUser("");
        src.setPassword("");
        return src;
    }
}
