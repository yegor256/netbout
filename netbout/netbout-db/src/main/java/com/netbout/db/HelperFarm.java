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
 * Manipulations with helpers.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Farm
public final class HelperFarm {

    /**
     * Find all helpers.
     * @return List of identities, which are helpers
     * @throws SQLException If some SQL problem inside
     */
    @Operation("get-all-helpers")
    public List<String> getAllHelpers()
        throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        final List<String> names = new ArrayList<String>();
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "SELECT identity FROM helper"
            );
            final ResultSet rset = stmt.executeQuery();
            try {
                while (rset.next()) {
                    names.add(rset.getString(1));
                }
            } finally {
                rset.close();
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#getAllHelpers(): retrieved %d identitie(s) [%dms]",
            names.size(),
            System.currentTimeMillis() - start
        );
        return names;
    }

    /**
     * Identity was promoted to helper.
     * @param name The name of identity
     * @param url URL of helper
     * @throws SQLException If some SQL problem inside
     */
    @Operation("identity-promoted")
    public void identityPromoted(final String name, final String url)
        throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "SELECT url FROM helper WHERE identity = ? "
            );
            stmt.setString(1, name);
            final ResultSet rset = stmt.executeQuery();
            try {
                if (rset.next()) {
                    final String existing = rset.getString(1);
                    if (!existing.equals(url)) {
                        throw new IllegalArgumentException(
                            String.format(
                                // @checkstyle LineLength (1 line)
                                "Identity '%s' is already promoted with '%s', can't change to '%s'",
                                name,
                                existing,
                                url
                            )
                        );
                    }
                } else {
                    final PreparedStatement istmt = conn.prepareStatement(
                        "INSERT INTO helper (identity, url) VALUES (?, ?)"
                    );
                    istmt.setString(1, name);
                    istmt.setString(2, url);
                    istmt.executeUpdate();
                    Logger.debug(
                        this,
                        "#identityPromoted('%s', '%s'): inserted [%dms]",
                        name,
                        url,
                        System.currentTimeMillis() - start
                    );
                }
            } finally {
                rset.close();
            }
        } finally {
            conn.close();
        }
    }

    /**
     * Get URL of the helper.
     * @param name The identity of bout participant
     * @return The URL
     * @throws SQLException If some SQL problem inside
     */
    @Operation("get-helper-url")
    public String getHelperUrl(final String name)
        throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        String url;
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "SELECT url FROM helper WHERE identity = ?"
            );
            stmt.setString(1, name);
            final ResultSet rset = stmt.executeQuery();
            try {
                if (!rset.next()) {
                    throw new IllegalArgumentException(
                        String.format(
                            "Identity '%s' not found, can't get helper's URL",
                            name
                        )
                    );
                }
                url = rset.getString(1);
            } finally {
                rset.close();
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#getHelperUrl('%s'): retrieved URL '%s' [%dms]",
            name,
            url,
            System.currentTimeMillis() - start
        );
        return url;
    }

}
