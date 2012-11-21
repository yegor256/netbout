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
package com.netbout.rest.rexsl.scripts

import com.jcabi.manifests.Manifests
import com.jcabi.urn.URN
import com.netbout.client.RestSession
import com.netbout.client.RestUriBuilder
import com.netbout.spi.Identity
import com.netbout.spi.text.SecureString
import com.rexsl.test.RestTester
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.UriBuilder
import org.mockito.Mockito

Manifests.append(new File(rexsl.basedir, 'src/test/resources/META-INF/MANIFEST.MF'))

def email = 'son@example.com'
def name = new URN('email', email)
def identity = Mockito.mock(Identity)
Mockito.doReturn(name).when(identity).name()
def secret = new SecureString(name).toString()
def son = new RestSession(rexsl.home).authenticate(name, secret)

def father = new RestSession(rexsl.home).authenticate(new URN('urn:test:father'), '')
def bout = father.start()
bout.post('Hi there!')
bout.invite(son)

def auth = RestTester.start(RestUriBuilder.from(son))
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .get('read from page to get AUTH param')
    .assertStatus(HttpURLConnection.HTTP_OK)
    .xpath('/page/auth/text()')
    .get(0)

def uri = UriBuilder.fromUri(rexsl.home)
    .path('/auth')
    .queryParam(RestSession.AUTH_PARAM, auth)
    .queryParam('identity', father.name())
    .queryParam('secret', '')

RestTester.start(uri)
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .get('logging in as father, in order to join with son')
    .assertStatus(HttpURLConnection.HTTP_SEE_OTHER)

RestTester.start(RestUriBuilder.from(father))
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .get('reading home page of main identity')
    .assertXPath("/page/identity[name='${father.name()}']")
    .assertXPath("/page/identity/aliases[alias='${email}']")
