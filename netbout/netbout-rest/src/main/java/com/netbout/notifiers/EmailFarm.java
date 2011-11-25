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
package com.netbout.notifiers;

import com.netbout.hub.HubEntry;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import com.netbout.spi.cpa.Farm;
import com.netbout.spi.cpa.IdentityAware;
import com.netbout.spi.cpa.Operation;
import com.netbout.utils.Cryptor;
import com.netbout.utils.TextUtils;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.UriBuilder;
import org.apache.velocity.VelocityContext;

/**
 * Email farm.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Farm
public final class EmailFarm {

    /**
     * Can notify identity?
     * @param name The name of identity
     * @return Can be notified by this farm or NULL if not
     */
    @Operation("can-notify-identity")
    public Boolean canNotifyIdentity(final String name) {
        Boolean can = null;
        if (this.isEmail(name)) {
            can = true;
        }
        Logger.debug(
            this,
            "#canNotifyIdentity('%s'): returned %B",
            name,
            can
        );
        return can;
    }

    /**
     * Notify bout participant.
     * @param number Number of bout
     * @param name The name of identity
     * @param since Latest date after which notification should be sent
     * @throws com.netbout.spi.BoutNotFoundException If bout number is invalid
     */
    @Operation("notify-bout-participant")
    public void notifyBoutParticipant(final Long number, final String name,
        final Date since) throws com.netbout.spi.BoutNotFoundException {
        if (this.isEmail(name)) {
            final Identity recepient = HubEntry.identity(name);
            final Bout bout = recepient.bout(number);
            final List<Message> recent = new ArrayList<Message>();
            for (Message message : bout.messages("")) {
                if (message.date().before(since)) {
                    continue;
                }
                if (!message.author().equals(recepient)) {
                    continue;
                }
                recent.add(message);
            }
            final VelocityContext context = new VelocityContext();
            context.put("bout", bout);
            context.put("recepient", recepient);
            context.put("recent", recent);
            context.put("since", since);
            context.put(
                "href",
                UriBuilder.fromUri("http://www.netbout.com/")
                    .path("/{num}")
                    .queryParam("auth", new Cryptor().encrypt(recepient))
                    .build(bout.number())
                    .toString()
            );
            final String text = TextUtils.format(
                "com/netbout/notifiers/email-notification.vm",
                context
            );
            this.deliver(recepient.name(), text);
        }
    }

    /**
     * Is it an email?
     * @param name The name of identity
     * @return Is it?
     */
    private Boolean isEmail(final String name) {
        return name.matches("[:\\w\\.\\-\\+]+@[\\w\\.\\-]+");
    }

    /**
     * Deliver this email.
     * @param email The address of recepient
     * @param The body
     */
    private void deliver(final String email, final String body) {
        Logger.info(
            this,
            "#deliver('%s', ..) sent:%n%s",
            email,
            body
        );
    }

}
