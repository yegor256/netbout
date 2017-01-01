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
import com.jcabi.jdbc.SingleOutcome;
import com.netbout.spi.Friend;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Cached friend.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.4
 */
@Immutable
@ToString
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = { "sql", "name" })
final class MkFriend implements Friend {

    /**
     * SQL data source provider.
     */
    private final transient Sql sql;

    /**
     * Name of it (alias).
     */
    private final transient String name;

    /**
     * Public ctor.
     * @param src Source
     * @param alias Alias
     */
    MkFriend(final Sql src, final String alias) {
        this.sql = src;
        this.name = alias;
    }

    @Override
    public String alias() {
        return this.name;
    }

    @Override
    public URI photo() throws IOException {
        try {
            return URI.create(
                new JdbcSession(this.sql.source())
                    .sql("SELECT photo FROM alias WHERE name = ?")
                    .set(this.name)
                    .select(new SingleOutcome<String>(String.class))
            );
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public String email() throws IOException {
        try {
            return new JdbcSession(this.sql.source())
                .sql("SELECT email FROM alias WHERE name = ?")
                .set(this.name)
                .select(new SingleOutcome<String>(String.class));
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
}
