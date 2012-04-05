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

import com.netbout.db.DbSession
import com.rexsl.core.Manifests
import com.ymock.util.Logger

def urlFile = new File(rexsl.basedir, 'jdbc.txt')
if (urlFile.exists()) {
    Manifests.inject('Netbout-JdbcUrl', urlFile.text.trim())
}

def line = new StringBuilder()
def queries = []
new File(rexsl.basedir, 'src/test/rexsl/start.sql').text.split('\n').each { text ->
    if (text.startsWith('--')) {
        return
    }
    line.append(text)
    if (text.trim().endsWith(';')) {
        queries.add(line.toString())
        line.setLength(0)
    }
}

// let's create a big amount of bouts and messages for one identity
(5000..6000).each {
    queries.add(
        'INSERT IGNORE INTO bout (number, title, date) VALUES'
        + " (${it}, 'test', '2001-01-01')"
    )
    queries.add(
        'INSERT IGNORE INTO participant (bout, identity, confirmed, date) VALUES'
        + " (${it}, 'urn:test:bumper', 1, '2001-01-01')"
    )
    queries.add(
        'INSERT IGNORE INTO message (number, bout, date, author, text) VALUES'
        + " (${it}, ${it}, '2001-01-01', 'urn:test:bumper', 'hi!')"
    )
}

queries.each { new DbSession(true).sql(it).update() }
Logger.info(this, 'Test database is ready (%d queries)', queries.size())
