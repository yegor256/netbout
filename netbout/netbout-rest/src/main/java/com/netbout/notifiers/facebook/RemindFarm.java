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
 */
package com.netbout.notifiers.facebook;

import com.netbout.spi.Urn;
import com.netbout.spi.cpa.Farm;
import com.netbout.spi.cpa.Operation;
import com.rexsl.core.Manifests;
import com.rexsl.test.RestTester;
import java.net.HttpURLConnection;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import org.hamcrest.Matchers;
import com.restfb.DefaultFacebookClient;
import com.restfb.Parameter;

/**
 * Reminder farm.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 * @see <a href="http://developers.facebook.com/docs/reference/api/user/#apprequests">Graph API</a>
 * @see <a href="http://developers.facebook.com/docs/authentication/#applogin">Authentication of apps</a>
 * @see <a href="http://developers.facebook.com/docs/reference/api/permissions/">App permissions</a>
 * @see <a href="http://stackoverflow.com/questions/6072839">related discussion in SO</a>
 * @see <a href="http://stackoverflow.com/questions/5758928">more about notifications</a>
 */
@Farm
public final class RemindFarm {

    /**
     * Remind identity which is silent for a long time.
     * @param name Name of identity
     * @param marker The marker to avoid duplicate reminders
     */
    @Operation("remind-silent-identity")
    public void remindSilentIdentity(final Urn name, final String marker) {
        final FacebookClient client = new DefaultFacebookClient(this.token());
        final String path = String.format("%s/apprequests", name.nss());
        final AppRequest reqs = client.fetchObject(path, AppRequest.class);
        client.publish(
            path,
            String.class,
            Parameter.with("data", marker),
            Parameter.with(
                "message",
                // @checkstyle LineLength (1 line)
                "There are a few messages waiting for your attention in Netbout.com"
            )
        );
    }

    /**
     * Get application access token.
     * @return The token
     */
    private String token() {
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
