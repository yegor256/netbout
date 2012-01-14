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

import com.netbout.spi.Urn;
import com.netbout.spi.cpa.Farm;
import com.netbout.spi.cpa.Operation;
import com.ymock.util.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Manipulations with bout participants.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Farm
public final class ParticipantFarm {

    /**
     * Get list of names of bout participants.
     * @param bout The number of the bout
     * @return List of names
     * @throws SQLException If some SQL problem inside
     */
    @Operation("get-bout-participants")
    public List<Urn> getBoutParticipants(final Long bout)
        throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        final List<Urn> names = new ArrayList<Urn>();
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                // @checkstyle LineLength (1 line)
                "SELECT identity FROM participant JOIN bout ON bout.number = participant.bout WHERE bout = ?"
            );
            stmt.setLong(1, bout);
            final ResultSet rset = stmt.executeQuery();
            try {
                while (rset.next()) {
                    names.add(Urn.create(rset.getString(1)));
                }
            } finally {
                rset.close();
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#getBoutParticipants('%s'): retrieved %d name(s) [%dms]",
            bout,
            names.size(),
            System.currentTimeMillis() - start
        );
        return names;
    }

    /**
     * Added new participant to the bout.
     * @param bout The bout
     * @param identity The name of the person
     * @throws SQLException If some SQL problem inside
     */
    @Operation("added-bout-participant")
    public void addedBoutParticipant(final Long bout, final Urn identity)
        throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                // @checkstyle LineLength (1 line)
                "INSERT INTO participant (bout, identity, date) VALUES (?, ?, ?)"
            );
            stmt.setLong(1, bout);
            stmt.setString(2, identity.toString());
            // @checkstyle MagicNumber (1 line)
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.execute();
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#addedBoutParticipant(#%d, '%s'): added [%dms]",
            bout,
            identity,
            System.currentTimeMillis() - start
        );
    }

    /**
     * Removed participant of the bout.
     * @param bout The bout
     * @param identity The name of the person
     * @throws SQLException If some SQL problem inside
     */
    @Operation("removed-bout-participant")
    public void removedBoutParticipant(final Long bout, final Urn identity)
        throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM participant WHERE bout = ? AND identity = ?"
            );
            stmt.setLong(1, bout);
            stmt.setString(2, identity.toString());
            stmt.execute();
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#removedBoutParticipant(#%d, '%s'): removed [%dms]",
            bout,
            identity,
            System.currentTimeMillis() - start
        );
    }

    /**
     * Get participant status.
     * @param bout The number of the bout
     * @param identity The participant
     * @return Status of the participant
     * @throws SQLException If some SQL problem inside
     */
    @Operation("get-participant-status")
    public Boolean getParticipantStatus(final Long bout, final Urn identity)
        throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        Boolean status;
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                // @checkstyle LineLength (1 line)
                "SELECT confirmed FROM participant WHERE bout = ? AND identity = ?"
            );
            stmt.setLong(1, bout);
            stmt.setString(2, identity.toString());
            final ResultSet rset = stmt.executeQuery();
            try {
                if (!rset.next()) {
                    throw new IllegalArgumentException(
                        String.format(
                            "Participant '%s' not found in bout #%d",
                            identity,
                            bout
                        )
                    );
                }
                status = rset.getBoolean(1);
            } finally {
                rset.close();
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#getParticipantStatus(#%d, '%s'): retrieved '%b' [%dms]",
            bout,
            identity,
            status,
            System.currentTimeMillis() - start
        );
        return status;
    }

    /**
     * Changed participant status.
     * @param bout The number of the bout
     * @param identity The participant
     * @param status The status to set
     * @throws SQLException If some SQL problem inside
     */
    @Operation("changed-participant-status")
    public void changedParticipantStatus(final Long bout,
        final Urn identity, final Boolean status) throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                // @checkstyle LineLength (1 line)
                "UPDATE participant SET confirmed = ? WHERE bout = ? AND identity = ?"
            );
            stmt.setBoolean(1, status);
            stmt.setLong(2, bout);
            // @checkstyle MagicNumber (1 line)
            stmt.setString(3, identity.toString());
            final int updated = stmt.executeUpdate();
            if (updated != 1) {
                throw new SQLException(
                    String.format(
                        "Participant #%d:'%s' not found, can't set status '%b'",
                        bout,
                        identity,
                        status
                    )
                );
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#changedParticipantStatus(#%d, '%s', %b): updated [%dms]",
            bout,
            identity,
            status,
            System.currentTimeMillis() - start
        );
    }

}
