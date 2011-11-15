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
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */

import com.netbout.db.Database
import com.rexsl.core.Manifests

Manifests.inject("Netbout-JdbcDriver", "com.mysql.jdbc.Driver")
Manifests.inject("Nebout-JdbcUrl", "jdbc:mysql://localhost:3306/netbout")
Manifests.inject("Nebout-JdbcUser", "netbout")
Manifests.inject("Nebout-JdbcPassword", "netbout")

def conn = Database.connection();
[
    "DELETE FROM message",
    "DELETE FROM participant",
    "DELETE FROM bout",
    "INSERT INTO bout (title) VALUES ('interesting discussion...')",
    "INSERT INTO participant (bout, identity, confirmed) VALUES (1, 'Axel Groom', 1)",
    "INSERT INTO participant (bout, identity, confirmed) VALUES (1, 'johnny.doe', 1)",
    "INSERT INTO message (bout, date, author, text) VALUES (1, '2011-11-15 03:18:34', 'Axel Groom', 'hi all!')",
    "INSERT INTO message (bout, date, author, text) VALUES (1, '2011-11-15 04:23:66', 'johnny.doe', 'works for me')",
].each { sql ->
    def stmt = conn.createStatement();
    stmt.execute(sql);
}
conn.close();
