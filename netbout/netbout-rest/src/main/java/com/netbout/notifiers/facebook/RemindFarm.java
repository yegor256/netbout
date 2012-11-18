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

import com.jcabi.urn.URN;
import com.netbout.spi.cpa.Farm;
import com.netbout.spi.cpa.Operation;
import com.restfb.DefaultFacebookClient;

/**
 * Reminder farm.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @see <a href="http://developers.facebook.com/docs/reference/api/user/#apprequests">Graph API</a>
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
    public void remindSilentIdentity(final URN name, final String marker) {
        final Requests requests = new Requests(
            new DefaultFacebookClient(new TokenBuilder().build()),
            name.nss()
        );
        if (requests.clean(marker)) {
            requests.publish(marker);
        }
    }

}
