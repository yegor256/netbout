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
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Seen statuses.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Farm
public final class SeenFarm {

    /**
     * Mark this message as seen by the specified identity.
     * @param msg The number of the message
     * @param identity The viewer
     * @throws SQLException If some SQL problem inside
     */
    @Operation("message-was-seen")
    public void messageWasSeen(final Long msg, final String identity)
        throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO seen (message, identity) VALUES (?, ?)"
            );
            stmt.setLong(1, msg);
            stmt.setString(2, identity);
            stmt.execute();
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#messageWasSeen(#%d, '%s'): inserted [%dms]",
            msg,
            identity,
            System.currentTimeMillis() - start
        );
    }

    /**
     * This message was seen by this identity?
     * @param msg The number of the message
     * @param identity The viewer
     * @return Was it seen?
     * @throws SQLException If some SQL problem inside
     */
    @Operation("was-message-seen")
    public Boolean wasMessageSeen(final Long msg, final String identity)
        throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        Boolean seen;
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "SELECT message FROM seen WHERE message = ? AND identity = ?"
            );
            stmt.setLong(1, msg);
            stmt.setString(2, identity);
            final ResultSet rset = stmt.executeQuery();
            try {
                if (rset.next()) {
                    seen = true;
                } else {
                    seen = false;
                }
            } finally {
                rset.close();
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#wasMessageSeen(#%d, '%s'): retrieved %b [%dms]",
            msg,
            identity,
            seen,
            System.currentTimeMillis() - start
        );
        return seen;
    }

}
