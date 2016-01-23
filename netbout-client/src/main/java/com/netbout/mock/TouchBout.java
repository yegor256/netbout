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
import java.io.IOException;
import java.sql.SQLException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Touch bout.
 *
 * @author Dmitry Zaytsev (dmitr.zaytsev@gmail.com)
 * @version $Id$
 * @since 2.23
 */
@Immutable
@ToString
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = "bout")
public final class TouchBout {
    /**
     * SQL data source provider.
     */
    private final transient Sql sql;

    /**
     * Bout.
     */
    private final transient long bout;

    /**
     * Ctor.
     * @param src Source
     * @param bot Bout number
     */
    TouchBout(final Sql src, final long bot) {
        this.sql = src;
        this.bout = bot;
    }

    /**
     * Set update attribute of the bout to current timestamp.
     */
    public void act() throws IOException {
        try {
            new JdbcSession(this.sql.source())
                // @checkstyle LineLength (1 line)
                .sql("UPDATE bout SET updated = CURRENT_TIMESTAMP WHERE number = ?")
                .set(this.bout)
                .update(Outcome.VOID);
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
}
