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
 * this code accidentally and without intent to use it, please report this
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
 */
package com.netbout.notifiers.facebook;

import com.jcabi.manifests.Manifests;
import com.rexsl.test.RestTester;
import java.net.HttpURLConnection;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import org.hamcrest.Matchers;

/**
 * Reminder farm.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @see <a href="http://developers.facebook.com/docs/authentication/#applogin">Authentication of apps</a>
 * @see <a href="http://developers.facebook.com/docs/reference/api/permissions/">App permissions</a>
 */
final class TokenBuilder {

    /**
     * Get application access token.
     * @return The token
     */
    public String build() {
        final URI uri = UriBuilder
            // @checkstyle MultipleStringLiterals (5 lines)
            .fromPath("https://graph.facebook.com/oauth/access_token")
            .queryParam("client_id", "{id}")
            .queryParam("client_secret", "{secret}")
            .queryParam("grant_type", "client_credentials")
            .build(
                Manifests.read("Netbout-FbId"),
                Manifests.read("Netbout-FbSecret")
            );
        final String response = RestTester.start(uri)
            .get("getting access_token from Facebook")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertBody(Matchers.startsWith("access_token="))
            .getBody();
        return response.split("=", 2)[1];
    }

}
