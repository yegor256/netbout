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
package com.netbout.hub;

import com.netbout.hub.queue.HelpQueue;
import com.netbout.spi.DuplicateIdentityException;
import com.netbout.spi.User;
import com.ymock.util.Logger;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Holder of all identities.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class Identities {

    /**
     * All identities known for us at the moment, and their objects.
     */
    private static final ConcurrentMap<String, HubIdentity> ALL =
        new ConcurrentHashMap<String, HubIdentity>();

    /**
     * It's a utility class.
     */
    private Identities() {
        // intentionally empty
    }

    /**
     * Make new identity or find existing one.
     * @param name The name of identity
     * @param user Name of the user
     * @return Identity found
     * @throws DuplicateIdentityException If this identity is taken
     * @checkstyle RedundantThrows (4 lines)
     */
    public static HubIdentity make(final String name, final User user)
        throws DuplicateIdentityException {
        final HubIdentity identity = Identities.make(name);
        if (identity.isAssigned() && !identity.belongsTo(user)) {
            throw new DuplicateIdentityException(
                "Identity '%s' is already taken by '%s'",
                name,
                identity.user().name()
            );
        }
        if (!identity.isAssigned()) {
            identity.assignTo(user);
        }
        return identity;
    }

    /**
     * Make new identity or find existing one.
     * @param name The name of identity
     * @return Identity found
     */
    public static HubIdentity make(final String name) {
        HubIdentity identity;
        if (Identities.ALL.containsKey(name)) {
            identity = Identities.ALL.get(name);
        } else {
            identity = new HubIdentity(name);
            Identities.ALL.put(name, identity);
            HelpQueue.make("identity-mentioned")
                .priority(HelpQueue.Priority.SYNCHRONOUSLY)
                .arg(name)
                .exec();
            Logger.debug(
                Identities.class,
                "#make('%s'): created just by name (%d total)",
                name,
                Identities.ALL.size()
            );
        }
        return identity;
    }

    /**
     * Find identities by name (including aliases).
     * @param keyword The keyword to find by
     * @return Identities found
     */
    public static Set<HubIdentity> findByKeyword(final String keyword) {
        final Set<HubIdentity> found = new HashSet<HubIdentity>();
        for (HubIdentity identity : Identities.ALL.values()) {
            if (identity.matchesKeyword(keyword)) {
                found.add(identity);
            }
        }
        final String[] external = HelpQueue.make("find-identities-by-keyword")
            .priority(HelpQueue.Priority.SYNCHRONOUSLY)
            .arg(keyword)
            .asDefault(new String[]{})
            .exec(String[].class);
        for (String name : external) {
            found.add(Identities.make(name));
        }
        return found;
    }

}
