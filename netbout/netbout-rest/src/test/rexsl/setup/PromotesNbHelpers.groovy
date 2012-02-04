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
package com.netbout.rest.rexsl.setup

import com.netbout.spi.Urn
import com.netbout.spi.client.RestSession
import com.netbout.spi.client.RestUriBuilder
import com.rexsl.test.RestTester
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.UriBuilder

def starter = new RestSession(rexsl.home).authenticate(new Urn(), 'localhost')
[
    'test' : '/mock-auth',
    'facebook': '/fb',
    'email': '/email', // doesn't work yet
].each {
    RestTester.start(RestUriBuilder.from(starter).build())
        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
        .get('home page of starter')
        .assertStatus(HttpURLConnection.HTTP_OK)
        .rel('//link[@rel="helper"]/@href')
        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
        .get('reading helper page')
        .rel('//link[@rel="namespaces"]/@href')
        .post(
            'register new namespace',
            'text=' + URLEncoder.encode(
                it.key + '=' + UriBuilder.fromUri(rexsl.home).path(it.value).build()
            )
        )
        .assertStatus(HttpURLConnection.HTTP_SEE_OTHER)
}

[
    'urn:test:hh' : 'file:com.netbout.hub.hh',
    'urn:test:bh' : 'file:com.netbout.bus.bh',
    'urn:test:email' : 'file:com.netbout.notifiers.email',
].each {
    def helper = new RestSession(rexsl.home).authenticate(new Urn(it.key), '')
    RestTester.start(RestUriBuilder.from(helper).build())
        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
        .get('home page')
        .assertStatus(HttpURLConnection.HTTP_OK)
        .rel('/page/links/link[@rel="helper"]/@href')
        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
        .get('reading promotion page')
        .rel('/page/links/link[@rel="promote"]/@href')
        .post('promoting helper', 'url=' + URLEncoder.encode(it.value))
        .assertStatus(HttpURLConnection.HTTP_SEE_OTHER)
}
