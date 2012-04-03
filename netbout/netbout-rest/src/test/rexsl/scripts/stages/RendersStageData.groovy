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
package com.netbout.rest.rexsl.scripts.stages

import com.netbout.spi.Urn
import com.netbout.spi.client.RestSession
import com.netbout.spi.client.RestUriBuilder
import com.rexsl.test.RestTester
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType

def jeff = new RestSession(rexsl.home).authenticate(new Urn('urn:test:jeff'), '')
def bout = jeff.start()
bout.post('hi there!')
def maria = new RestSession(rexsl.home).authenticate(new Urn('urn:test:maria'), '')
bout.rename('Rendering urn:test:hh stage data')
bout.invite(maria)
bout.invite(jeff.friend(new Urn('urn:facebook:1531296526')))
bout.invite(jeff.friend(new Urn('urn:test:hh')))

// validate that the stage is really there, in XHTML
RestTester.start(RestUriBuilder.from(bout))
    .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML)
    .get('read bout page')
    .assertStatus(HttpURLConnection.HTTP_OK)
    .assertXPath('//xhtml:div[@id="stage"]//xhtml:p')

// validate the same stage from Maria's point of view
RestTester.start(RestUriBuilder.from(maria.bout(bout.number())))
    .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML)
    .get('read bout page for Maria')
    .assertStatus(HttpURLConnection.HTTP_OK)
    .assertXPath('//xhtml:div[@id = "stage"]//xhtml:p')
