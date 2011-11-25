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
package com.netbout.rest.rexsl.scripts

import com.netbout.harness.CookieBuilder
import com.rexsl.test.TestClient
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.UriBuilder

def cookie = CookieBuilder.cookie()
def friend = "7265734"

// start new bout
def bout = new XmlSlurper()
    .parseText(
        new TestClient(rexsl.home)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
            .header(HttpHeaders.COOKIE, cookie)
            .get('/s')
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath('/page/bout/number')
            .body
    )
    .bout
    .number

// invite friend to the bout
new TestClient(rexsl.home)
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .header(HttpHeaders.COOKIE, cookie)
    .get(
        UriBuilder.fromPath('/{bout}/i')
            .queryParam('name', identity)
            .build(bout)
            .toString()
    )
    .assertStatus(HttpURLConnection.HTTP_MOVED_PERM)

// post new message to this bout, but a friend (using "auth" param)
new TestClient(rexsl.home)
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .body('text=How are you?')
    .post(
        UriBuilder.fromPath('/{bout}/p')
            .queryParam('auth', CookieBuilder.auth(friend))
            .build(bout)
            .toString()
    )
    .assertStatus(HttpURLConnection.HTTP_MOVED_PERM)

// let's check that it exists there, the message
new TestClient(rexsl.home)
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .header(HttpHeaders.COOKIE, cookie)
    .get(
        UriBuilder.fromPath('/{bout}')
            .build(bout)
            .toString()
    )
    .assertStatus(HttpURLConnection.HTTP_OK)
    .assertXPath("/processing-instruction('xml-stylesheet')[contains(.,'/bout.xsl')]")
    .assertXPath('/page/identity/name[.="${friend}"]')
    .assertXPath('/page/bout/title[.=""]')
    .assertXPath('/page/bout/participants/participant')
    .assertXPath('/page/bout/messages/message/text[.="How are you?"]')
