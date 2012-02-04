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
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Manipulations on the level of identity.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Farm
public final class IdentityFarm {

    /**
     * Find identities by keyword.
     * @param keyword The keyword
     * @return List of identities
     */
    @Operation("find-identities-by-keyword")
    public List<Urn> findIdentitiesByKeyword(final String keyword) {
        final String matcher = String.format(
            "%%%s%%",
            keyword.toUpperCase(Locale.ENGLISH)
        );
        return new DbSession().sql(
            // @checkstyle StringLiteralsConcatenation (8 lines)
            "SELECT identity.name FROM identity "
            + "LEFT JOIN alias ON alias.identity = identity.name "
            + "WHERE identity.name = ? OR "
            + "(UCASE(alias.name) LIKE ? AND"
            + " (identity.name LIKE 'urn:facebook:%' OR"
            + " identity.name LIKE 'urn:test:%'))"
            + "GROUP BY identity.name "
            + "LIMIT 10"
        )
        .set(keyword.toUpperCase(Locale.ENGLISH))
        .set(matcher)
        .select(
            new Handler<List<Urn>>() {
                @Override
                public List<Urn> handle(final ResultSet rset)
                    throws SQLException {
                    List<Urn> names = null;
                    while (rset.next()) {
                        if (names == null) {
                            names = new ArrayList<Urn>();
                        }
                        names.add(Urn.create(rset.getString(1)));
                    }
                    return names;
                }
            }
        );
    }

    /**
     * Get list of bouts that belong to some identity.
     * @param name The identity of bout participant
     * @return List of bout numbers
     */
    @Operation("get-bouts-of-identity")
    public List<Long> getBoutsOfIdentity(final Urn name) {
        return new DbSession()
            // @checkstyle LineLength (1 line)
            .sql("SELECT number FROM bout JOIN participant ON bout.number = participant.bout WHERE identity = ?")
            .set(name)
            .select(
                new Handler<List<Long>>() {
                    @Override
                    public List<Long> handle(final ResultSet rset)
                        throws SQLException {
                        final List<Long> numbers = new ArrayList<Long>();
                        while (rset.next()) {
                            numbers.add(rset.getLong(1));
                        }
                        return numbers;
                    }
                }
            );
    }

    /**
     * Get identity photo.
     * @param name The name of the identity
     * @return Photo of the identity
     */
    @Operation("get-identity-photo")
    public URL getIdentityPhoto(final Urn name) {
        return new DbSession()
            .sql("SELECT photo FROM identity WHERE name = ?")
            .set(name)
            .select(
                new Handler<URL>() {
                    @Override
                    public URL handle(final ResultSet rset)
                        throws SQLException {
                        if (!rset.next()) {
                            throw new IllegalArgumentException(
                                String.format(
                                    "Identity '%s' not found, can't read photo",
                                    name
                                )
                            );
                        }
                        try {
                            return new URL(rset.getString(1));
                        } catch (java.net.MalformedURLException ex) {
                            throw new IllegalStateException(ex);
                        }
                    }
                }
            );
    }

    /**
     * Identity was mentioned in the app and should be registed here.
     * @param name The name of identity
     */
    @Operation("identity-mentioned")
    public void identityMentioned(final Urn name) {
        final Boolean exists = new DbSession()
            .sql("SELECT name FROM identity WHERE name = ?")
            .set(name)
            .select(new NotEmptyHandler());
        if (!exists) {
            new DbSession()
                // @checkstyle LineLength (1 line)
                .sql("INSERT INTO identity (name, photo, date) VALUES (?, ?, ?)")
                .set(name)
                .set("http://img.netbout.com/unknown.png")
                .set(new Date())
                .insert(new VoidHandler());
        }
    }

    /**
     * Changed identity photo.
     * @param name The name of identity
     * @param photo The photo to set
     */
    @Operation("changed-identity-photo")
    public void changedIdentityPhoto(final Urn name, final URL photo) {
        new DbSession()
            .sql("UPDATE identity SET photo = ? WHERE name = ?")
            .set(photo)
            .set(name)
            .update();
    }

}
