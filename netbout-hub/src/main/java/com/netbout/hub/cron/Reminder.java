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
package com.netbout.hub.cron;

import com.jcabi.log.Logger;
import com.jcabi.urn.URN;
import com.netbout.hub.PowerHub;
import java.util.ArrayList;
import java.util.List;

/**
 * Remind all identities.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class Reminder extends AbstractCron {

    /**
     * Public ctor.
     * @param hub The hub
     */
    public Reminder(final PowerHub hub) {
        super(hub);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void call() throws Exception {
        final List<URN> names = this.hub().make("find-silent-identities")
            .synchronously()
            .asDefault(new ArrayList<URN>(0))
            .exec();
        for (URN name : names) {
            this.remind(name);
        }
        return null;
    }

    /**
     * Remind one person.
     * @param name The name to remind
     */
    private void remind(final URN name) {
        final String marker = this.hub().make("get-silence-marker")
            .synchronously()
            .arg(name)
            .asDefault("")
            .exec();
        if (marker.isEmpty()) {
            Logger.warn(
                this,
                "#remind(%s): has to be reminded but marker is empty",
                name
            );
        } else {
            this.hub().make("remind-silent-identity")
                .arg(name)
                .arg(marker)
                .asDefault(false)
                .exec();
            Logger.info(
                this,
                "#remind(%s): has to be reminded: '%s'",
                name,
                marker
            );
        }
    }

}
