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
package com.netbout.rest.rexsl.bumper

import com.netbout.spi.Query
import com.netbout.spi.Urn
import com.netbout.spi.client.RestSession
import com.netbout.spi.client.RestUriBuilder
import com.rexsl.test.RestTester
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.UriBuilder
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

def paul = new RestSession(rexsl.home).authenticate(new Urn('urn:test:paul'), '')

def bout = paul.start()
bout.rename('Posting XML messages to bumper')
bout.post('hi there!')
bout.invite(paul.friend(new Urn('urn:test:bumper')))

def xsd = UriBuilder.fromUri(rexsl.home).path('/bumper/ns.xsd').build()
RestTester.start(RestUriBuilder.from(bout).path('/s'))
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .post(
        'post message to the bumper',
        'data=' + URLEncoder.encode(
            """<bump xmlns='urn:test:bumper:ns?bar=%E8%94%94%20value%3F'
                xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'
                xsi:schemaLocation='urn:test:bumper:ns?bar=%E8%94%94%20value%3F ${xsd}' >
                <text>hello, dude!</text>
                <text>hello again!</text>
            </bump>"""
        )
    )
    .assertStatus(HttpURLConnection.HTTP_SEE_OTHER)
MatcherAssert.assertThat(
    bout.messages(
        new Query.Textual(
            '(and (ns "urn:test:bumper:ns?bar=%E8%94%94%20value%3F") (pos 0))'
        )
    ),
    Matchers.hasSize(1)
)
