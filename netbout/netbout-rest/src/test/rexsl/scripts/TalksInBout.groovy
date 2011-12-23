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

import com.netbout.spi.Urn
import com.netbout.spi.client.RestSession
import com.netbout.spi.client.RestUriBuilder
import com.rexsl.test.RestTester
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

def mandy = new RestSession(rexsl.home).authenticate(new Urn('urn:test:mandy'), '')
def bob = new RestSession(rexsl.home).authenticate(new Urn('urn:test:bob'), '')

def bout = mandy.start()
bout.rename('Some test conversation between Jeff and Walter, \u0443\u0440\u0430!')
def number = bout.number()
bout.invite(bob)
bob.bout(number).confirm()
def text = 'Hi, dude!'
def msg = bob.bout(number).post(text).number()
MatcherAssert.assertThat(bout.message(msg).text(), Matchers.equalTo(text))

// validate content of the bout page
RestTester.start(RestUriBuilder.from(bout))
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .get()
    .assertStatus(HttpURLConnection.HTTP_OK)
    .assertXPath('/page/identity[name="urn:test:mandy"]')
    .assertXPath('/page/bout/participants/participant[identity="urn:test:mandy"]')
    .assertXPath('/page/bout/participants/participant[identity="urn:test:bob"]')
    .assertXPath('/page/bout/messages/message[author="urn:test:bob"]')
    .assertXPath('/page/bout/messages/message[text="Hi, dude!"]')
