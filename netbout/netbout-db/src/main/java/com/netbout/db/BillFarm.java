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

import com.netbout.spi.cpa.Farm;
import com.netbout.spi.cpa.Operation;
import com.ymock.util.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Manipulations with bills.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Farm
public final class BillFarm {

    /**
     * Save a collection of incoming bills, from BUS.
     * @param lines Text forms of them
     * @throws SQLException If some SQL problem inside
     */
    @Operation("save-bills")
    public void saveBills(final List<String> lines) throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        try {
            for (String line : lines) {
                final PreparedStatement stmt = conn.prepareStatement(
                    // @checkstyle LineLength (1 line)
                    "INSERT INTO bill (date, mnemo, helper, msec, bout) VALUES (?, ?, ?, ?, ?)"
                );
                final String[] parts = line.split("[ ]+");
                Utc.setTimestamp(
                    stmt,
                    1,
                    ISODateTimeFormat.dateTime()
                        .parseDateTime(parts[0])
                        .toDate()
                );
                stmt.setString(2, parts[1]);
                // @checkstyle MagicNumber (6 lines)
                stmt.setString(3, parts[2]);
                stmt.setLong(4, Long.valueOf(parts[3]));
                if ("null".equals(parts[4])) {
                    stmt.setString(5, null);
                } else {
                    stmt.setLong(5, Long.valueOf(parts[4]));
                }
                try {
                    stmt.execute();
                } finally {
                    stmt.close();
                }
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#saveBills(%d bills): saved [%dms]",
            lines.size(),
            System.currentTimeMillis() - start
        );
    }

}
