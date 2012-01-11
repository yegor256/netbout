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
import java.util.ArrayList;
import java.util.List;

/**
 * Manipulations with namespaces.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Farm
public final class NamespaceFarm {

    /**
     * Namespace was registered.
     * @param owner The owner of it
     * @param name The name of namespace
     * @param template The template of it
     * @throws SQLException If some SQL problem inside
     * @checkstyle ExecutableStatementCount (70 lines)
     */
    @Operation("namespace-was-registered")
    public void namespaceWasRegistered(final Urn owner, final String name,
        final String template) throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "SELECT name FROM namespace WHERE name = ?"
            );
            stmt.setString(1, name);
            final ResultSet rset = stmt.executeQuery();
            try {
                if (rset.next()) {
                    final PreparedStatement istmt = conn.prepareStatement(
                        // @checkstyle LineLength (1 line)
                        "UPDATE namespace SET identity = ?, template = ? WHERE name = ?"
                    );
                    istmt.setString(1, owner.toString());
                    istmt.setString(2, template);
                    // @checkstyle MagicNumber (1 line)
                    istmt.setString(3, name);
                    istmt.executeUpdate();
                    Logger.debug(
                        this,
                        // @checkstyle LineLength (1 line)
                        "#namespaceWasRegistered('%s', '%s', '%s'): updated [%dms]",
                        owner,
                        name,
                        template,
                        System.currentTimeMillis() - start
                    );
                } else {
                    final PreparedStatement istmt = conn.prepareStatement(
                        // @checkstyle LineLength (1 line)
                        "INSERT INTO namespace (name, identity, template, date) VALUES (?, ?, ?, ?)"
                    );
                    istmt.setString(1, name);
                    istmt.setString(2, owner.toString());
                    // @checkstyle MagicNumber (1 line)
                    istmt.setString(3, template);
                    istmt.setDate(
                        // @checkstyle MagicNumber (1 line)
                        4,
                        new java.sql.Date(System.currentTimeMillis())
                    );
                    istmt.executeUpdate();
                    Logger.debug(
                        this,
                        // @checkstyle LineLength (1 line)
                        "#namespaceWasRegistered('%s', '%s', '%s'): inserted [%dms]",
                        owner,
                        name,
                        template,
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
     * Find all namespaces.
     * @return List of them
     * @throws SQLException If some SQL problem inside
     */
    @Operation("get-all-namespaces")
    public List<String> getAllNamespaces() throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        final List<String> names = new ArrayList<String>();
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "SELECT name FROM namespace"
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
            "#getAllNamespaces(): retrieved %d namespace(s) [%dms]",
            names.size(),
            System.currentTimeMillis() - start
        );
        return names;
    }

    /**
     * Get owner of namespace.
     * @param name The name of namespace
     * @return Photo of the identity
     * @throws SQLException If some SQL problem inside
     */
    @Operation("get-namespace-owner")
    public Urn getNamespaceOwner(final String name) throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        Urn owner;
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "SELECT identity FROM namespace WHERE name = ?"
            );
            stmt.setString(1, name);
            final ResultSet rset = stmt.executeQuery();
            try {
                if (!rset.next()) {
                    throw new IllegalArgumentException(
                        String.format(
                            "Namespace '%s' not found, can't read owner",
                            name
                        )
                    );
                }
                owner = Urn.create(rset.getString(1));
            } finally {
                rset.close();
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#getNamespaceOwner('%s'): retrieved '%s' [%dms]",
            name,
            owner,
            System.currentTimeMillis() - start
        );
        return owner;
    }

    /**
     * Get template of namespace.
     * @param name The name of namespace
     * @return Photo of the identity
     * @throws SQLException If some SQL problem inside
     */
    @Operation("get-namespace-template")
    public String getNamespaceTemplate(final String name) throws SQLException {
        final long start = System.currentTimeMillis();
        final Connection conn = Database.connection();
        String template;
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "SELECT template FROM namespace WHERE name = ?"
            );
            stmt.setString(1, name);
            final ResultSet rset = stmt.executeQuery();
            try {
                if (!rset.next()) {
                    throw new IllegalArgumentException(
                        String.format(
                            "Namespace '%s' not found, can't read template",
                            name
                        )
                    );
                }
                template = rset.getString(1);
            } finally {
                rset.close();
            }
        } finally {
            conn.close();
        }
        Logger.debug(
            this,
            "#getNamespaceTemplate('%s'): retrieved '%s' [%dms]",
            name,
            template,
            System.currentTimeMillis() - start
        );
        return template;
    }

}
