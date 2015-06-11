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
import com.netbout.spi.Attachments;
import com.netbout.spi.Bout;
import com.netbout.spi.Friends;
import com.netbout.spi.Messages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Cached bout.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.4
 */
@Immutable
@ToString
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = { "sql", "bout", "self" })
final class MkBout implements Bout {

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
    MkBout(final Sql src, final long bot, final String slf) {
        this.sql = src;
        this.bout = bot;
        this.self = slf;
    }

    @Override
    public long number() {
        return this.bout;
    }

    @Override
    public Date date() throws IOException {
        try {
            return new JdbcSession(this.sql.source())
                .sql("SELECT date FROM bout WHERE number = ?")
                .set(this.bout)
                .select(new SingleOutcome<Date>(Date.class));
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public Date updated() throws IOException {
        throw new UnsupportedOperationException("#updated()");
    }

    @Override
    public String title() throws IOException {
        try {
            return new JdbcSession(this.sql.source())
                .sql("SELECT title FROM bout WHERE number = ?")
                .set(this.bout)
                .select(new SingleOutcome<String>(String.class));
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void rename(final String text) throws IOException {
        try {
            new JdbcSession(this.sql.source())
                .sql("UPDATE bout SET title = ? WHERE number = ?")
                .set(text)
                .set(this.bout)
                .update(Outcome.VOID);
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public boolean subscription() throws IOException {
        try {
            return new JdbcSession(this.sql.source())
                // @checkstyle LineLength (1 lines)
                .sql("SELECT subscription FROM friend WHERE bout = ? and alias = ?")
                .set(this.bout)
                .set(this.self)
                .select(new SingleOutcome<Boolean>(Boolean.class));
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void subscribe(final boolean subs) throws IOException {
        try {
            new JdbcSession(this.sql.source())
                // @checkstyle LineLength (1 lines)
                .sql("UPDATE friend SET subscription = ? WHERE bout = ? and alias = ?")
                .set(subs)
                .set(this.bout)
                .set(this.self)
                .update(Outcome.VOID);
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public Messages messages() {
        return new MkMessages(this.sql, this.bout, this.self);
    }

    @Override
    public Friends friends() {
        return new MkFriends(this.sql, this.bout);
    }

    @Override
    public Attachments attachments() {
        return new MkAttachments(this.sql, this.bout, this.self);
    }
}
