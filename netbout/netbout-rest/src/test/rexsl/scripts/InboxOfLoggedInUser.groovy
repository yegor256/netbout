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

import com.rexsl.test.TestClient
import com.rexsl.test.XhtmlConverter
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import org.junit.Assert
import org.xmlmatchers.XmlMatchers
import org.hamcrest.Matchers

// user name: John Doe
// identity name: johnny.doe
def cookie = 'netbout="Sm9obiBEb2U=.am9obm55LmRvZQ==.97febcab64627f2ebc4bb9292c3cc0bd"'

new TestClient(rexsl.home)
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .header(HttpHeaders.COOKIE, cookie)
    .get('/s')
// redirect is happening here, but we don't catch it
// @see http://trac.fazend.com/rexsl/ticket/53
// Assert.assertThat(r1.status, Matchers.equalTo(HttpURLConnection.HTTP_MOVED_TEMP))

def r2 = new TestClient(rexsl.home)
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .header(HttpHeaders.COOKIE, cookie)
    .get('/')
Assert.assertThat(r2.status, Matchers.equalTo(HttpURLConnection.HTTP_OK))
[
    "/processing-instruction('xml-stylesheet')[contains(.,'/inbox.xsl')]",
    '/page/identity/name[.="johnny.doe"]',
    '/page/bouts',
    '/page/bouts/bout/participants/participant',
].each {
