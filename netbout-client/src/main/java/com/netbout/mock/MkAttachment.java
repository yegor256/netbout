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
import com.jcabi.jdbc.Outcome;
import com.jcabi.jdbc.SingleOutcome;
import com.netbout.spi.Attachment;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;

/**
 * Cached attachment.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.4
 */
@Immutable
@ToString
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = { "sql", "bout", "label" })
final class MkAttachment implements Attachment {

    /**
     * SQL data source provider.
     */
    private final transient Sql sql;

    /**
     * Bout.
     */
    private final transient long bout;

    /**
     * Attachment name.
     */
    private final transient String label;

    /**
     * Public ctor.
     * @param src Source
     * @param bot Bout number
     * @param name Name
     */
    MkAttachment(final Sql src, final long bot, final String name) {
        this.sql = src;
        this.bout = bot;
        this.label = name;
    }

    @Override
    public String name() {
        return this.label;
    }

    @Override
    public String ctype() throws IOException {
        try {
            return new JdbcSession(this.sql.source())
                .sql("SELECT ctype FROM attachment WHERE bout = ? AND name = ?")
                .set(this.bout)
                .set(this.label)
                .select(new SingleOutcome<>(String.class));
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public String etag() throws IOException {
        try {
            return new JdbcSession(this.sql.source())
                .sql("SELECT etag FROM attachment WHERE bout = ? AND name = ?")
                .set(this.bout)
                .set(this.label)
                .select(new SingleOutcome<>(String.class));
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public boolean unseen() {
        return false;
    }

    @Override
    public Date date() throws IOException {
        try {
            return new JdbcSession(this.sql.source())
                .sql("SELECT date FROM attachment WHERE bout = ? AND name = ?")
                .set(this.bout)
                .set(this.label)
                .select(
                    new Outcome<Date>() {
                        @Override
                        public Date handle(final ResultSet rset,
                            final Statement stmt) throws SQLException {
                            rset.next();
                            return new Date(rset.getTimestamp(1).getTime());
                        }
                    }
                );
        } catch (final SQLException ex) {
            throw new IOException(
                String.format("Can't fetch date from bout #%d", this.bout),
                ex
            );
        }
    }

    @Override
    public String author() throws IOException {
        try {
            return new JdbcSession(this.sql.source())
                .sql(
                    "SELECT author FROM attachment WHERE bout = ? AND name = ?"
                )
                .set(this.bout)
                .set(this.label)
                .select(new SingleOutcome<>(String.class));
        } catch (final SQLException ex) {
            throw new IOException(
                String.format("Can't fetch author from bout #%d", this.bout),
                ex
            );
        }
    }

    @Override
    public InputStream read() throws IOException {
        try {
            return IOUtils.toInputStream(
                new JdbcSession(this.sql.source())
                    // @checkstyle LineLength (1 line)
                    .sql("SELECT data FROM attachment WHERE bout = ? AND name = ?")
                    .set(this.bout)
                    .set(this.label)
                    .select(new SingleOutcome<String>(String.class)),
                StandardCharsets.UTF_8
            );
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void write(final InputStream stream, final String ctype,
        final String etag) throws IOException {
        try {
            new JdbcSession(this.sql.source())
                // @checkstyle LineLength (1 line)
                .sql("UPDATE attachment SET data = ?, ctype = ?, etag = ? WHERE bout = ? AND name = ?")
                .set(IOUtils.toString(stream, StandardCharsets.UTF_8))
                .set(ctype)
                .set(etag)
                .set(this.bout)
                .set(this.label)
                .update(Outcome.VOID);
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
        new TouchBout(this.sql, this.bout).act();
    }
}
