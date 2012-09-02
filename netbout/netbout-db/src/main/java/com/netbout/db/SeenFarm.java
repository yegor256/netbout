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
package com.netbout.db;

import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.NotEmptyHandler;
import com.jcabi.jdbc.Utc;
import com.jcabi.jdbc.VoidHandler;
import com.netbout.spi.Urn;
import com.netbout.spi.cpa.Farm;
import com.netbout.spi.cpa.Operation;

/**
 * Seen statuses.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Farm
public final class SeenFarm {

    /**
     * Mark this message as seen by the specified identity.
     * @param msg The number of the message
     * @param identity The viewer
     */
    @Operation("message-was-seen")
    public void messageWasSeen(final Long msg, final Urn identity) {
        new JdbcSession(Database.source())
            .sql("INSERT INTO seen (message, identity, date) VALUES (?, ?, ?)")
            .set(msg)
            .set(identity)
            .set(new Utc())
            .insert(new VoidHandler());
    }

    /**
     * This message was seen by this identity?
     * @param msg The number of the message
     * @param identity The viewer
     * @return Was it seen?
     */
    @Operation("was-message-seen")
    public Boolean wasMessageSeen(final Long msg, final Urn identity) {
        return new JdbcSession(Database.source())
            .sql("SELECT message FROM seen WHERE message = ? AND identity = ?")
            .set(msg)
            .set(identity)
            .select(new NotEmptyHandler());
    }

}
