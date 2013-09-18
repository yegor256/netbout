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
package com.netbout.db;

import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.NotEmptyHandler;
import com.jcabi.jdbc.SingleHandler;
import com.jcabi.jdbc.Utc;
import com.jcabi.jdbc.VoidHandler;
import com.jcabi.urn.URN;
import com.netbout.spi.cpa.Farm;
import com.netbout.spi.cpa.Operation;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Manipulations with helpers.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Farm
public final class HelperFarm {

    /**
     * Find all helpers.
     * @return List of identities, which are helpers
     * @throws SQLException If fails
     */
    @Operation("get-all-helpers")
    public List<URN> getAllHelpers() throws SQLException {
        return new JdbcSession(Database.source())
            .sql("SELECT identity FROM helper")
            .select(
                new JdbcSession.Handler<List<URN>>() {
                    @Override
                    public List<URN> handle(final ResultSet rset)
                        throws SQLException {
                        final List<URN> names = new LinkedList<URN>();
                        while (rset.next()) {
                            names.add(URN.create(rset.getString(1)));
                        }
                        return names;
                    }
                }
            );
    }

    /**
     * Identity was promoted to helper.
     * @param name The name of identity
     * @param url URL of helper
     * @throws SQLException If fails
     */
    @Operation("identity-promoted")
    public void identityPromoted(final URN name, final URL url)
        throws SQLException {
        final Boolean exists = new JdbcSession(Database.source())
            .sql("SELECT url FROM helper WHERE identity = ? ")
            .set(name)
            .select(new NotEmptyHandler());
        if (exists) {
            final URL existing = this.getHelperUrl(name);
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
            new JdbcSession(Database.source())
                // @checkstyle LineLength (1 line)
                .sql("INSERT INTO helper (identity, url, date) VALUES (?, ?, ?)")
                .set(name)
                .set(url)
                .set(new Utc())
                .insert(new VoidHandler());
        }
    }

    /**
     * Get URL of the helper.
     * @param name The identity of bout participant
     * @return The URL
     * @throws SQLException If fails
     */
    @Operation("get-helper-url")
    public URL getHelperUrl(final URN name) throws SQLException {
        final String location = new JdbcSession(Database.source())
            .sql("SELECT url FROM helper WHERE identity = ?")
            .set(name)
            .select(new SingleHandler<String>(String.class));
        URL url;
        try {
            url = new URL(location);
        } catch (java.net.MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
        return url;
    }

}
