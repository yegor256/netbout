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
package com.netbout.db.helper;

import com.netbout.db.Database;
import com.ymock.util.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
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
     * Collection of totals.
     * @return The totals
     * @throws SQLException If some SQL problem inside
     */
    @XmlElement(name = "total")
    @XmlElementWrapper(name = "totals")
    public Collection<Total> getTotals() throws SQLException {
        final Collection<Total> totals = new ArrayList<Total>();
        final String[] tables = new String[] {
            "identity",
            "bout",
            "message",
            "helper",
            "alias",
            "participant",
        };
        for (String table : tables) {
            final Long count = Long.valueOf(
                this.query(String.format("SELECT COUNT(*) FROM %s", table))
            );
            totals.add(new Total(table, count));
        }
        return totals;
    }

    /**
     * Build text summary.
     * @param sql SQL query with single expected result
     * @return The result
     * @throws SQLException If some SQL problem inside
     */
    private String query(final String sql) throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        String result;
        try {
            final PreparedStatement stmt = conn.prepareStatement(sql);
            final ResultSet rset = stmt.executeQuery();
            try {
                if (!rset.next()) {
                    throw new IllegalArgumentException(
                        String.format("Nothing for %s", sql)
                    );
                }
                result = rset.getString(1);
            } finally {
                rset.close();
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#query('%s'): retrieved %s [%dms]",
            sql,
            result,
            System.currentTimeMillis() - start
        );
        return result;
    }

}
