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
package com.netbout.dh;

import com.netbout.db.DbSession;
import com.netbout.db.Handler;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The stage to render with JAXB.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlType(name = "data")
@XmlAccessorType(XmlAccessType.NONE)
public final class Stage {

    /**
     * The query to render (or empty if nothing to render).
     */
    private final transient String requested;

    /**
     * Public ctor, for JAXB.
     */
    public Stage() {
        throw new IllegalStateException("illegal call");
    }

    /**
     * Public ctor.
     * @param sql The query to render
     */
    public Stage(final String sql) {
        if (sql == null) {
            this.requested = "";
        } else {
            this.requested = sql;
        }
    }

    /**
     * Get the text.
     * @return The text.
     * @throws SQLException If some SQL problem inside
     */
    @XmlElement
    @SuppressWarnings({
        "PMD.AvoidCatchingGenericException",
        "PMD.AvoidInstantiatingObjectsInLoops"
    })
    public String getText() throws SQLException {
        final StringBuilder text = new StringBuilder();
        final String[] queries = new String[] {
            this.requested,
            "SELECT COUNT(*) FROM alias",
            "SELECT COUNT(*) FROM bill",
            "SELECT COUNT(*) FROM bout",
            "SELECT COUNT(*) FROM helper",
            "SELECT COUNT(*) FROM identity",
            "SELECT COUNT(*) FROM invoice",
            "SELECT COUNT(*) FROM message",
            "SELECT COUNT(*) FROM namespace",
            "SELECT COUNT(*) FROM seen",
            "SELECT * FROM invoice ORDER BY `day` DESC LIMIT 5",
            "SHOW EVENTS",
            "SHOW PROCESSLIST",
        };
        final Handler<String> handler = new DataHandler();
        for (String query : queries) {
            if (query.isEmpty()) {
                continue;
            }
            text.append(query).append(":\n");
            try {
                text.append(new DbSession(true).sql(query).select(handler));
            // @checkstyle IllegalCatch (1 line)
            } catch (Exception ex) {
                text.append(ex.getMessage()).append(" \n");
            }
        }
        return text.toString();
    }

    /**
     * The handler.
     */
    private static final class DataHandler implements Handler<String> {
        /**
         * {@inheritDoc}
         */
        @Override
        public String handle(final ResultSet rset) throws SQLException {
            final StringBuilder text = new StringBuilder();
            final int total = rset.getMetaData().getColumnCount();
            int row = 0;
            while (rset.next()) {
                // @checkstyle MagicNumber (1 line)
                if (row > 50) {
                    text.append("  and more...");
                    break;
                }
                row += 1;
                text.append("  ");
                for (int col = 0; col < total; col += 1) {
                    text.append(rset.getString(col + 1))
                        .append(" | ");
                }
                text.append("\n");
            }
            return text.toString();
        }
    }

}
