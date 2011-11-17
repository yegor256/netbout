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
import java.util.ArrayList;
import java.util.List;

/**
 * Manipulations on the level of identity.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Farm
public final class IdentityFarm {

    /**
     * Get list of bouts that belong to some identity.
     * @param name The identity of bout participant
     * @return List of bout numbers
     * @throws SQLException If some SQL problem inside
     */
    @Operation("get-bouts-of-identity")
    public Long[] getBoutsOfIdentity(final String name) throws SQLException {
        final Connection conn = Database.connection();
        final List<Long> numbers = new ArrayList<Long>();
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                // @checkstyle LineLength (1 line)
                "SELECT number FROM bout JOIN participant ON bout.number = participant.bout WHERE identity = ?"
            );
            stmt.setString(1, name);
            final ResultSet rset = stmt.executeQuery();
            while (rset.next()) {
                numbers.add(rset.getLong(1));
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#getBoutsOfIdentity('%s'): retrieved %d bout number(s)",
            name,
            numbers.size()
        );
        return numbers.toArray(new Long[]{});
    }

    /**
     * Get identity photo.
     * @param name The name of the identity
     * @return Photo of the identity
     * @throws SQLException If some SQL problem inside
     */
    @Operation("get-identity-photo")
    public String getIdentityPhoto(final String name)
        throws SQLException {
        final Connection conn = Database.connection();
        String photo;
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "SELECT photo FROM identity WHERE name = ?"
            );
            stmt.setString(1, name);
            final ResultSet rset = stmt.executeQuery();
            rset.next();
            photo = rset.getString(1);
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#getIdentityPhoto('%s'): retrieved '%s'",
            name,
            photo
        );
        return photo;
    }

    /**
     * Identity was mentioned in the app and should be registed here.
     * @param name The name of identity
     * @throws SQLException If some SQL problem inside
     */
    @Operation("identity-mentioned")
    public void identityMentioned(final String name) throws SQLException {
        final Connection conn = Database.connection();
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "SELECT name FROM identity WHERE name = ?"
            );
            stmt.setString(1, name);
            final ResultSet rset = stmt.executeQuery();
            if (!rset.next()) {
                final PreparedStatement istmt = conn.prepareStatement(
                    "INSERT INTO identity (name, photo) VALUES (?, ?)"
                );
                istmt.setString(1, name);
                istmt.setString(2, "http://img.netbout.com/unknown.png");
                istmt.executeUpdate();
                Logger.debug(
                    this,
                    "#identityMentioned('%s'): inserted",
                    name
                );
            }
        } finally {
            conn.close();
        }
    }

    /**
     * Changed identity photo.
     * @param name The name of identity
     * @param photo The photo to set
     * @throws SQLException If some SQL problem inside
     */
    @Operation("changed-identity-photo")
    public void changedIdentityPhoto(final String name,
        final String photo) throws SQLException {
        this.identityMentioned(name);
        final Connection conn = Database.connection();
        try {
            final PreparedStatement ustmt = conn.prepareStatement(
                "UPDATE identity SET photo = ? WHERE name = ?"
            );
            ustmt.setString(1, photo);
            ustmt.setString(2, name);
            ustmt.executeUpdate();
            Logger.debug(
                this,
                "#changedIdentityPhoto('%s', '%s'): updated",
                name,
                photo
            );
        } finally {
            conn.close();
        }
    }

}
