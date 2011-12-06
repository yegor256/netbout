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

import com.netbout.rest.CookieMocker
import com.rexsl.test.TestClient
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.UriBuilder

def cookie = new CookieMocker().cookie()
def email = 'test@example.com'

// start new bout
def suggestURI = new TestClient(rexsl.home)
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .header(HttpHeaders.COOKIE, cookie)
    .followRedirects(true)
    .get('/s')
    .assertStatus(HttpURLConnection.HTTP_OK)
    .gpath
    .links.link.find { it.@rel == 'suggest' }.@href.toURI()

// get suggestions about who we can invite
def inviteURI = new TestClient(UriBuilder.fromUri(suggestURI).queryParam('k', email).build())
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .header(HttpHeaders.COOKIE, cookie)
    .get()
    .assertStatus(HttpURLConnection.HTTP_OK)
    .assertXPath("/page/keyword[.='${email}']")
    .assertXPath("/page/invitees/invitee[name='${email}']")
    .gpath
    .invitees.invitee.find { it.name == email }.@href.toURI()

// invite this email and get URI to post a message
def postURI = new TestClient(inviteURI)
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .header(HttpHeaders.COOKIE, cookie)
    .followRedirects(true)
    .get()
    .assertStatus(HttpURLConnection.HTTP_OK)
    .assertXPath("/page/bout/participants/participant[identity='${email}']")
    .gpath
    .links.link.find { it.@rel == 'post' }.@href.toURI()

// post new message to this bout
new TestClient(postURI)
    .header(HttpHeaders.COOKIE, cookie)
    .body('text=How are you?')
    .post()
    .assertStatus(HttpURLConnection.HTTP_SEE_OTHER)
