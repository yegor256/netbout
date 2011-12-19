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

import com.rexsl.test.RestTester
import com.ymock.server.YMockServer
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriBuilder
import org.apache.http.client.utils.URLEncodedUtils

RestTester.start(rexsl.home)
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .get()
    .assertStatus(Response.Status.TEMPORARY_REDIRECT.statusCode)
    .follow()
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .get()
    .assertStatus(HttpURLConnection.HTTP_OK)
    .assertXPath('/page/links/link[@rel="facebook"]')
    .gpath
    .links.link.find { it.@rel == 'facebook' }.@href.toURI()

/**
@todo #163 This mechanism doesn't work, because YMOCK can't properly
 mock sockets at the moment. Working on this...

def redirect = URLEncodedUtils.parse(fburi, 'UTF-8').find { it.name == 'redirect_uri' }.value
new YMockServer('com.ymock.mock.socket:graph.facebook.com')
    .when('.*GET.*', '{id: "64372382"}')
RestTester.start(UriBuilder.fromUri(redirect).queryParam('code', 'secret-facebook-code'))
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .get()
    .assertStatus(HttpURLConnection.HTTP_SEE_OTHER)
*/