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
 */
package com.netbout.hub;

import com.netbout.bus.Bus;
import com.netbout.spi.Urn;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.List;

/**
 * Entry point to Hub.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.DoNotUseThreads")
final class Reminder implements Runnable {

    /**
     * The hub.
     */
    private final transient Bus ibus;

    /**
     * Public ctor.
     * @param bus The bus
     */
    public Reminder(final Bus bus) {
        this.ibus = bus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        final List<Urn> names = this.ibus.make("find-silent-identities")
            .synchronously()
            .asDefault(new ArrayList<Urn>(0))
            .exec();
        for (Urn name : names) {
            this.remind(name);
        }
    }

    /**
     * Remind one person.
     * @param name The name to remind
     */
    private void remind(final Urn name) {
        final String marker = this.ibus.make("get-silence-marker")
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
            this.ibus.make("remind-silent-identity")
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
