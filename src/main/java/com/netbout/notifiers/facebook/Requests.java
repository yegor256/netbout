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

import com.jcabi.log.Logger;
import com.restfb.Connection;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;

/**
 * Manager of AppRequests.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @see <a href="http://developers.facebook.com/docs/reference/base/user/#apprequests">Graph API</a>
 * @see <a href="http://developers.facebook.com/docs/authentication/#applogin">Authentication of apps</a>
 * @see <a href="http://developers.facebook.com/docs/reference/base/permissions/">App permissions</a>
 * @see <a href="http://stackoverflow.com/questions/6072839">related discussion in SO</a>
 * @see <a href="http://stackoverflow.com/questions/5758928">more about notifications</a>
 */
final class Requests {

    /**
     * The client.
     */
    private final transient FacebookClient client;

    /**
     * The path in FB Graph API.
     */
    private final transient String path;

    /**
     * Public ctor.
     * @param clnt The client
     * @param fbid Facebook ID
     */
    public Requests(final FacebookClient clnt, final String fbid) {
        this.client = clnt;
        this.path = String.format("%s/apprequests", fbid);
    }

    /**
     * Public apprequest.
     * @param marker The marker to avoid duplicate reminders
     */
    public void publish(final String marker) {
        this.client.publish(
            this.path,
            FacebookType.class,
            Parameter.with("data", marker),
            Parameter.with("message", String.format("%s waiting", marker))
        );
        Logger.info(
            this,
            "#publish('%s'): published to %s",
            marker,
            this.path
        );
    }

    /**
     * Clean some previous apprequests (or all of them).
     * @param marker The marker to avoid duplicate reminders (or empty string
     *  if you want ALL apprequests to be cleaned for this person)
     * @return Is it clean now and we should post again?
     */
    @SuppressWarnings("PMD.CloseResource")
    public boolean clean(final String marker) {
        final Connection<AppRequest> reqs =
            this.client.fetchConnection(this.path, AppRequest.class);
        boolean clean = true;
        if (!reqs.getData().isEmpty()) {
            for (AppRequest request : reqs.getData()) {
                if (request.getData().equals(marker) && !marker.isEmpty()) {
                    clean = false;
                } else {
                    this.client.deleteObject(request.getId());
                    Logger.info(
                        this,
                        "#clean('%s'): deleted apprequest %s",
                        marker,
                        request.getId()
                    );
                }
            }
        }
        Logger.info(this, "#clean('%s'): %B", marker, clean);
        return clean;
    }

}
