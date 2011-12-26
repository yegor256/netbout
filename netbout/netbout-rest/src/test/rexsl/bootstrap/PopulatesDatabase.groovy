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
package com.netbout.rest.rexsl.bootstrap

import com.netbout.db.Database
import com.rexsl.core.Manifests
import com.ymock.util.Logger

def driver = 'com.mysql.jdbc.Driver'
def url = 'jdbc:mysql://test-db.netbout.com:3306/netbout-test?useUnicode=true&characterEncoding=utf-8'
def user = 'netbout-test'
def password = 'secret'

def urlFile = new File('./jdbc.txt')
if (urlFile.exists()) {
    url = urlFile.text
}

Manifests.inject('Netbout-JdbcDriver', driver)
Manifests.inject('Netbout-JdbcUrl', url)
Manifests.inject('Netbout-JdbcUser', user)
Manifests.inject('Netbout-JdbcPassword', password)

def conn = Database.connection()
[
    'DELETE FROM namespace WHERE identity != "urn:void:"',
    'DELETE FROM helper',
    'DELETE FROM alias',
    'DELETE FROM seen',
    'DELETE FROM message',
    'DELETE FROM participant',
    'DELETE FROM bout',
    'DELETE FROM identity WHERE name != "urn:void:"',
    """INSERT INTO identity (name, photo) VALUES ('urn:facebook:4466',
        'http://www.topnews.in/light/files/John-Turturro.jpg')""",
    """INSERT INTO identity (name, photo) VALUES ('urn:test:cindy', 'http://www.localhost')""",
    "INSERT INTO alias (identity, name) VALUES ('urn:facebook:4466', 'John Turturro')",
    "INSERT INTO bout (number, title) VALUES (555, '\u0443\u0440\u0430!')",
    "INSERT INTO participant (bout, identity, confirmed) VALUES (555, 'urn:facebook:4466', 1)",
    "INSERT INTO participant (bout, identity, confirmed) VALUES (555, 'urn:test:cindy', 1)",
    """INSERT INTO message (bout, date, author, text) VALUES
        (555, '2011-11-15 03:18:34', 'urn:facebook:4466', 'first message')""",
    """INSERT INTO message (bout, date, author, text) VALUES
        (555, '2011-11-15 04:28:22', 'urn:test:cindy', 'second message')""",
    """INSERT INTO message (bout, date, author, text) VALUES
        (555, '2011-11-15 05:23:11', 'urn:test:cindy', '\u0443!')""",
    """INSERT INTO namespace (name, identity, template) VALUES
        ('foo', 'urn:facebook:4466', 'http://localhost/foo')""",
].each { query ->
    def stmt = conn.createStatement()
    stmt.execute(query)
    Logger.debug(this, 'SQL executed: %s', query)
}
conn.close()
Logger.info(this, 'Test database is ready at %s', url)
