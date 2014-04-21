/**
 * Copyright (c) 2009-2014, Netbout.com
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
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
package com.netbout.rest.rexsl.scripts

import com.jcabi.urn.URN
import com.netbout.client.EtaAssertion
import com.netbout.client.RestSession
import com.netbout.client.RestUriBuilder
import com.rexsl.test.RestTester
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType

def andre = new RestSession(rexsl.home).authenticate(new URN('urn:test:andre'), '')
def bout = andre.start()

RestTester.start(RestUriBuilder.from(bout))
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .get('read bout front page')
    .assertStatus(HttpURLConnection.HTTP_OK)
    .assertThat(new EtaAssertion())
    .rel('/page/links/link[@rel="post"]/@href')
    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
    .post('posts message to the bout', 'text=' + URLEncoder.encode('how are you?'))
    .assertStatus(HttpURLConnection.HTTP_SEE_OTHER)
    .follow()
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .get('read bout page again')
    .assertStatus(HttpURLConnection.HTTP_OK)
    .assertThat(new EtaAssertion())
    .assertXPath('/page/bout/messages[count(message) = 1]')
    .assertXPath('/page/bout/messages/message[text="how are you?"]')
    .assertXPath('/page/bout/messages/message[@seen="true"]')
    .assertXPath('/page/bout/messages/message[author="urn:test:andre"]')
    .assertXPath('/page/bout/messages/message/when')
    .assertXPath('/page/bout/messages/message/date')
    .assertXPath('/page/bout/messages/message/number')
    .assertXPath('/page/bout/messages/message/render')
    .assertXPath('/page/log[count(event) > 0]')
    .assertXPath('/page/log[count(event[.=""]) = 0]')
