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

import com.netbout.spi.Identity;
import com.ymock.util.Logger;
import java.util.Set;

/**
 * Entry point to Hub.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class HubEntry {

    /**
     * It's a utility class.
     */
    private HubEntry() {
        // empty
    }

    /**
     * Find user by name.
     * @param name The name of the user to find
     * @return The user found
     */
    public static HubUser user(final String name) {
        final HubUser user = new HubUser(name);
        Logger.debug(
            HubEntry.class,
            "#user('%s'): instantiated",
            name
        );
        return user;
    }

    /**
     * Find identities by name.
     * @param keyword The keyword
     * @return Set of identities found
     */
    public static Set<Identity> find(final String keyword) {
        final Set<Identity> identities =
            (Set) Identities.findByKeyword(keyword);
        Logger.debug(
            HubEntry.class,
            "#find('%s'): found %d identities",
            keyword,
            identities.size()
        );
        return identities;
    }

    /**
     * Register new notifier.
     * @param notifier The notifier to use
     */
    public static void register(final HubNotifier notifier) {
        Notifiers.register(notifier);
        Logger.debug(
            HubEntry.class,
            "#register('%s'): registered",
            notifier.getClass().getName()
        );
    }

}
