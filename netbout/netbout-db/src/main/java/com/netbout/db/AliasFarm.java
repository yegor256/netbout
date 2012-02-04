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
 * Manipulations with aliases.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Farm
public final class AliasFarm {

    /**
     * Primary alias was added to the identity.
     * @param identity The identity
     * @param alias The alias just added
     */
    @Operation("added-identity-alias")
    public void addedIdentityAlias(final Urn identity, final String alias) {
        new DbSession()
            .sql("INSERT INTO alias (identity, name, date) VALUES (?, ?, ?)")
            .set(identity)
            .set(alias)
            .set(new Date())
            .insert(new VoidHandler());
    }

    /**
     * Get list of aliases that belong to some identity.
     * @param name The identity of bout participant
     * @return List of aliases
     */
    @Operation("get-aliases-of-identity")
    public List<String> getAliasesOfIdentity(final Urn name) {
        return new DbSession()
            .sql("SELECT name FROM alias WHERE identity = ?")
            .set(name)
            .select(
                new Handler<List<String>>() {
                    @Override
                    public List<String> handle(final ResultSet rset)
                        throws SQLException {
                        List<String> aliases = null;
                        while (rset.next()) {
                            if (aliases == null) {
                                aliases = new ArrayList<String>();
                            }
                            aliases.add(rset.getString(1));
                        }
                        return aliases;
                    }
                }
            );
    }

}
