/**
 * Copyright (c) 2009-2011, NetBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi;

import com.ymock.util.Logger;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Utils for netbout entities manipulations.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class NetboutUtils {

    /**
     * It's a utility class.
     */
    private NetboutUtils() {
        // empty
    }

    /**
     * Get alias of identity.
     * @param identity The identity
     * @return The alias
     */
    public static String aliasOf(final Identity identity) {
        final Iterator<String> iter = identity.aliases().iterator();
        String alias;
        if (iter.hasNext()) {
            alias = iter.next();
        } else {
            alias = identity.name().toString();
        }
        return alias;
    }

    /**
     * Get the latest date of the particular bout (when it was updated by
     * anyone).
     * @param bout The bout to check
     * @return Its recent date
     */
    public static Date dateOf(final Bout bout) {
        final List<Message> msgs = bout.messages("(pos 0)");
        Date recent = bout.date();
        if (!msgs.isEmpty()) {
            final Date mdate = msgs.get(0).date();
            if (mdate.before(recent)) {
                throw new IllegalArgumentException(
                    String.format(
                        // @checkstyle LineLength (1 line)
                        "Message #%d in bout #%d created on '%s', which before bout was created '%s', how come?",
                        msgs.get(0).number(),
                        bout.number(),
                        mdate,
                        recent
                    )
                );
            }
            recent = mdate;
        }
        return recent;
    }

    /**
     * Find a person in the bout, if he's there (otherwise throw a runtime
     * exception).
     * @param identity The person to find
     * @param bout Where to find
     * @return The participant
     */
    public static Participant participantOf(final Identity identity,
        final Bout bout) {
        final Collection<Participant> participants = bout.participants();
        Participant found = null;
        for (Participant participant : participants) {
            if (participant.identity().name().equals(identity.name())) {
                found = participant;
            }
        }
        if (found == null) {
            throw new IllegalStateException(
                Logger.format(
                    "Can't find myself ('%s') among %d participants: %[list]s",
                    identity,
                    participants.size(),
                    participants
                )
            );
        }
        return found;
    }

    /**
     * Checks whether this person participates in the bout.
     * @param name Name of the person
     * @param bout Where to find
     * @return He is in?
     */
    public static boolean participatesIn(final Urn name,
        final Bout bout) {
        final Collection<Participant> participants = bout.participants();
        boolean found = false;
        for (Participant participant : participants) {
            if (participant.identity().name().equals(name)) {
                found = true;
                break;
            }
        }
        return found;
    }

}
