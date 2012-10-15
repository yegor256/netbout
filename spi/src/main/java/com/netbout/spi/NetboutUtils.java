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

import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Utils for netbout entities manipulations.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @todo #176 This entire class is a temporary workaround. It contains static
 *  functions, which should be transformed into normal class methods.
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
        final Pattern pattern = Pattern.compile(
            "\\s*\\(.*\\)\\s*", Pattern.DOTALL
        );
        String normalized;
        if (query == null) {
            normalized = NetboutUtils.normalize("");
        } else if (pattern.matcher(query).matches()) {
            normalized = query.trim();
        } else {
            normalized = String.format(
                "(matches '%s')",
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
        // @checkstyle MultipleStringLiterals (1 line)
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
        final Iterable<Message> msgs = bout.messages("(pos 0)");
        boolean seen = true;
        if (msgs.iterator().hasNext()) {
            seen = msgs.iterator().next().seen();
        }
        return !seen;
    }

}
