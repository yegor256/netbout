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
package com.netbout.rest.rexsl.scripts.catapult

// This script is disabled since it doesn't work in Linux Ubuntu
// for some strange reason.
/*
import com.jcabi.manifests.Manifests
import com.jcabi.urn.URN
import com.netbout.client.RestSession
import com.netbout.client.RestUriBuilder
import com.netbout.spi.text.SecureString
import com.rexsl.test.RestTester
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.UriBuilder

Manifests.append(new File(rexsl.basedir, 'src/test/resources/META-INF/MANIFEST.MF'))

def home = UriBuilder.fromUri(System.getProperty('catapult.home'))
    .scheme('https')
    .port(Integer.valueOf(System.getProperty('catapult.https.port')))
    .build()

def starter = new RestSession(home, '9OLKJ8JHGytfh6JGJF0LKF').authenticate(new URN(), '')
def text = 'netbout=' + UriBuilder.fromUri(home).path('/nb').build()
RestTester.start(RestUriBuilder.from(starter).build())
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .get('read home page in SSL')
    .assertStatus(HttpURLConnection.HTTP_OK)
    .assertXPath('/page[@ssl="true"]')
    .rel('/page/links/link[@rel="profile"]/@href')
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .get('read profile')
    .rel('/page/links/link[@rel="namespaces"]/@href')
    .post('register namespace', 'text=' + URLEncoder.encode(text))
    .assertStatus(HttpURLConnection.HTTP_SEE_OTHER)

def name = new URN('urn:netbout:sisily')
def sisily = new RestSession(home).authenticate(name, new SecureString(name).toString())
this.prepare(RestUriBuilder.from(sisily).build())
def bout = sisily.start()
bout.rename('Catapult SSL testing')

private void prepare(URI path) {
    RestTester.start(path)
        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
        .get('read SSL home page of sisily')
        .assertStatus(HttpURLConnection.HTTP_OK)
        .rel('/page/links/link[@rel="start"]/@href')
        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
        .get('start new SSL bout for Sisily')
        .assertStatus(HttpURLConnection.HTTP_SEE_OTHER)
        .follow()
        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
        .get('read SSL bout page')
        .assertStatus(HttpURLConnection.HTTP_OK)
        .assertXPath('/page[@ssl = "true"]')
        .rel('/page/links/link[@rel="post"]/@href')
        .post('post new SSL message', 'text=hello+SSL')
        .assertStatus(HttpURLConnection.HTTP_SEE_OTHER)
}
*/
