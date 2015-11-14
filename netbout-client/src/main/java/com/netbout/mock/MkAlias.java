/**
 * Copyright (c) 2009-2015, netbout.com
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
import com.netbout.spi.Alias;
import com.netbout.spi.Bout;
import com.netbout.spi.Inbox;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.Locale;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Cached alias.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.4
 */
@Immutable
@ToString
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = { "sql", "label" })
final class MkAlias implements Alias {

    /**
     * SQL data source provider.
     */
    private final transient Sql sql;

    /**
     * His name.
     */
    private final transient String label;

    /**
     * Public ctor.
     * @param src Source
     * @param name Alias
     */
    MkAlias(final Sql src, final String name) {
        this.sql = src;
        this.label = name;
    }

    @Override
    public String name() {
        return this.label;
    }

    @Override
    public URI photo() throws IOException {
        try {
            return URI.create(
                new JdbcSession(this.sql.source())
                    .sql("SELECT photo FROM alias WHERE name = ?")
                    .set(this.label)
                    .select(new SingleOutcome<String>(String.class))
            );
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public Locale locale() throws IOException {
        try {
            return new Locale(
                new JdbcSession(this.sql.source())
                    .sql("SELECT locale FROM alias WHERE name = ?")
                    .set(this.label)
                    .select(new SingleOutcome<String>(String.class))
            );
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void photo(final URI uri) throws IOException {
        try {
            new JdbcSession(this.sql.source())
                .sql("UPDATE alias SET photo = ? WHERE name = ?")
                .set(uri)
                .set(this.label)
                .update(Outcome.VOID);
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public String email() throws IOException {
        try {
            return new JdbcSession(this.sql.source())
                .sql("SELECT email FROM alias WHERE name = ?")
                .set(this.label)
                .select(new SingleOutcome<String>(String.class));
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void email(final String email) throws IOException {
        try {
            new JdbcSession(this.sql.source())
                .sql("UPDATE alias SET email = ? WHERE name = ?")
                .set(email)
                .set(this.label)
                .update(Outcome.VOID);
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void email(final String email, final String urn, final Bout bout)
        throws IOException {
        this.email(email);
    }

    @Override
    public void email(final String email, final String link)
        throws IOException {
        this.email(email);
    }

    @Override
    public Inbox inbox() throws IOException {
        return new MkInbox(this.sql, this.label);
    }
}
