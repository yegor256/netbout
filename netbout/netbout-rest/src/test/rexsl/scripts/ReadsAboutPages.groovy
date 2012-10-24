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
 * @version $Id: PostsInvalidMessages.groovy 3219 2012-08-14 10:54:12Z yegor@tpc2.com $
 */
package com.netbout.rest.rexsl.scripts

import com.rexsl.test.RestTester
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType

RestTester.start(rexsl.home)
    .get('read front page')
    .follow()
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .get('read next page after forwarding')
    .assertStatus(HttpURLConnection.HTTP_OK)
    .rel('/page/links/link[@rel="about"]/@href')
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .get('read about again')
    .assertStatus(HttpURLConnection.HTTP_OK)
    .assertXPath('/page/content')
