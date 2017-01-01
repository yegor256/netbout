/**
 * Copyright (c) 2009-2017, netbout.com
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
import com.netbout.spi.Attachment;
import com.netbout.spi.Attachments;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Cached attachments.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.4
 */
@Immutable
@ToString
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = { "sql", "bout", "self" })
final class MkAttachments implements Attachments {

    /**
     * SQL data source provider.
     */
    private final transient Sql sql;

    /**
     * Bout.
     */
    private final transient long bout;

    /**
     * Self alias.
     */
    private final transient String self;

    /**
     * Public ctor.
     * @param src Source
     * @param bot Bout number
     * @param slf Self alias
     */
    MkAttachments(final Sql src, final long bot, final String slf) {
        this.sql = src;
        this.bout = bot;
        this.self = slf;
    }

    // @todo #806:30min/DEV This method needs to be implemented
    //  because it is used in tests that read bout properties.
    //  Currently, this implementation always returns 0.
    @Override
    public int unseen() throws IOException {
        return 0;
    }

    @Override
    public void create(final String name) throws IOException {
        try {
            new JdbcSession(this.sql.source())
                // @checkstyle LineLength (1 line)
                .sql("INSERT INTO attachment (name, bout, author, ctype, etag) VALUES (?, ?, ?, ?, ?)")
                .set(name)
                .set(this.bout)
                .set(this.self)
                .set("text/plain")
                .set("000")
                .insert(Outcome.VOID);
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void delete(final String name) throws IOException {
        try {
            new JdbcSession(this.sql.source())
                .sql("DELETE FROM attachment WHERE name = ? AND bout = ?")
                .set(name)
                .set(this.bout)
                .insert(Outcome.VOID);
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public Attachment get(final String name) {
        return new MkAttachment(this.sql, this.bout, name);
    }

    @Override
    public Iterable<Attachment> iterate() throws IOException {
        try {
            return new JdbcSession(this.sql.source())
                .sql("SELECT name FROM attachment WHERE bout = ?")
                .set(this.bout)
                .select(
                    new Outcome<Iterable<Attachment>>() {
                        @Override
                        public Iterable<Attachment> handle(final ResultSet rset,
                            final Statement stmt) throws SQLException {
                            final Collection<Attachment> list =
                                new LinkedList<>();
                            while (rset.next()) {
                                list.add(
                                    new MkAttachment(
                                        MkAttachments.this.sql,
                                        MkAttachments.this.bout,
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
