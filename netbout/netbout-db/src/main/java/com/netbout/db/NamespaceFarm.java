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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
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
     */
    @Operation("namespace-was-registered")
    public void namespaceWasRegistered(final Urn owner, final String name,
        final String template) {
        final Boolean exists = new DbSession(true)
            .sql("SELECT name FROM namespace WHERE name = ?")
            .set(name)
            .select(new NotEmptyHandler());
        if (exists) {
            new DbSession(true)
                // @checkstyle LineLength (1 line)
                .sql("UPDATE namespace SET identity = ?, template = ? WHERE name = ?")
                .set(owner)
                .set(template)
                .set(name)
                .update();
        } else {
            new DbSession(true)
                // @checkstyle LineLength (1 line)
                .sql("INSERT INTO namespace (name, identity, template, date) VALUES (?, ?, ?, ?)")
                .set(name)
                .set(owner)
                .set(template)
                .set(new Date())
                .insert(new VoidHandler());
        }
    }

    /**
     * Find all namespaces.
     * @return List of them
     */
    @Operation("get-all-namespaces")
    public List<String> getAllNamespaces() {
        return new DbSession(true)
            .sql("SELECT name FROM namespace")
            .select(
                new Handler<List<String>>() {
                    @Override
                    public List<String> handle(final ResultSet rset)
                        throws SQLException {
                        final List<String> names = new ArrayList<String>();
                        while (rset.next()) {
                            names.add(rset.getString(1));
                        }
                        return names;
                    }
                }
            );
    }

    /**
     * Get owner of namespace.
     * @param name The name of namespace
     * @return Photo of the identity
     */
    @Operation("get-namespace-owner")
    public Urn getNamespaceOwner(final String name) {
        return new DbSession(true)
            .sql("SELECT identity FROM namespace WHERE name = ?")
            .set(name)
            .select(
                new Handler<Urn>() {
                    @Override
                    public Urn handle(final ResultSet rset)
                        throws SQLException {
                        if (!rset.next()) {
                            throw new IllegalArgumentException(
                                String.format(
                                    "Namespace '%s' not found can't read owner",
                                    name
                                )
                            );
                        }
                        return Urn.create(rset.getString(1));
                    }
                }
            );
    }

    /**
     * Get template of namespace.
     * @param name The name of namespace
     * @return Photo of the identity
     */
    @Operation("get-namespace-template")
    public String getNamespaceTemplate(final String name) {
        return new DbSession(true)
            .sql("SELECT template FROM namespace WHERE name = ?")
            .set(name)
            .select(
                new Handler<String>() {
                    @Override
                    public String handle(final ResultSet rset)
                        throws SQLException {
                        if (!rset.next()) {
                            throw new IllegalArgumentException(
                                String.format(
                                    "Namespace '%s' not found, can't read tmpl",
                                    name
                                )
                            );
                        }
                        return rset.getString(1);
                    }
                }
            );
    }

}
