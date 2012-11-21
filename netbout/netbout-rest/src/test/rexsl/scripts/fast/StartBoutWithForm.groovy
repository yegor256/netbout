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
package com.netbout.rest.rexsl.scripts.fast

import com.jcabi.urn.URN
import com.netbout.client.EtaAssertion
import com.netbout.client.RestSession
import com.netbout.client.RestUriBuilder
import com.rexsl.test.RestTester
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.UriBuilder
import org.apache.commons.lang.CharEncoding

def message = 'Hi, how are you doing there?\nI\'m fine by the way!\n'
def first = 'urn:test:bratt'
def second = 'urn:test:mickey'

def bruce = new RestSession(rexsl.home).authenticate(new URN('urn:test:bruce'), '')

def uri = UriBuilder.fromUri(RestUriBuilder.from(bruce).build())
    .path('/fast/start')
    .build(String.format('%s,%s', first, second), message)

RestTester.start(uri)
    .post(
        'starting a bout',
        String.format(
            'participants=%s,%s&message=%s&leader=1',
            URLEncoder.encode(first, CharEncoding.UTF_8),
            URLEncoder.encode(second, CharEncoding.UTF_8),
            URLEncoder.encode(message, CharEncoding.UTF_8)
        )
    )
    .assertStatus(HttpURLConnection.HTTP_SEE_OTHER)
    .follow()
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .get('read the bout just created')
    .assertStatus(HttpURLConnection.HTTP_OK)
    .assertThat(new EtaAssertion())
    .assertXPath('/page/bout/participants/participant[identity="urn:test:bruce"]')
    .assertXPath('//participant[identity="urn:test:bruce" and @leader="false"]')
    .assertXPath('//participant[identity="urn:test:bratt" and @leader="true"]')
    .assertXPath('//participant[identity="urn:test:mickey" and @leader="false"]')
    .assertXPath('//messages/message[contains(text, "how are you doing")]')
