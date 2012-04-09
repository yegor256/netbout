/**
 * Copyright (c) 2009-2012, Netbout.com
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
import java.util.Set;

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
        final Profile profile = identity.profile();
        assert profile != null : "Profile is NULL";
        final Set<String> aliases = profile.aliases();
        assert aliases != null : "Set of aliases in the profile is NULL";
        final Iterator<String> iter = aliases.iterator();
        String alias;
        if (iter.hasNext()) {
            alias = iter.next();
        } else {
            alias = identity.name().toString();
        }
        return alias;
    }

    /**
     * Normalize the query.
     * @param query Raw format
     * @return The text for predicate
     */
    public static String normalize(final String query) {
        String normalized;
        if (query == null) {
            normalized = NetboutUtils.normalize("");
        } else if (!query.isEmpty() && query.charAt(0) == '('
            && query.endsWith(")")) {
            normalized = query;
        } else {
            normalized = String.format(
                // @checkstyle LineLength (1 line)
                "(or (matches '%s' $text) (matches '%1$s' $bout.title) (matches '%1$s' $author.alias))",
                query.replace("'", "\\'")
            );
        }
        return normalized;
    }

    /**
     * Get the latest date of the particular bout (when it was updated by
     * anyone).
     * @param bout The bout to check
     * @return Its recent date
     */
    public static Date dateOf(final Bout bout) {
        final Iterable<Message> msgs = bout.messages("(pos 0)");
        Date recent = bout.date();
        if (msgs.iterator().hasNext()) {
            final Message msg = msgs.iterator().next();
            final Date mdate = msg.date();
            if (mdate.before(recent)) {
                throw new IllegalArgumentException(
                    String.format(
                        // @checkstyle LineLength (1 line)
                        "Message #%d in bout #%d created on '%s', which before bout was created '%s', how come?",
                        msg.number(),
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
     * This bout has something unread?
     * @param bout The bout to check
     * @return TRUE if this bout has some new messages
     * @todo #332 This implementation is very ineffective and should be
     *  changed. We should introduce a new predicate in INF and use it.
     *  Something like "(unseen-by ?)".
     */
    public static boolean isUnread(final Bout bout) {
        boolean unread = false;
        for (Message msg : bout.messages("")) {
            if (!msg.seen()) {
                unread = true;
                break;
            }
        }
        return unread;
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
                    // @checkstyle LineLength (1 line)
                    "Can't find myself ('%s') among %d participants in bout #%d: %[list]s",
                    identity,
                    participants.size(),
                    bout.number(),
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
